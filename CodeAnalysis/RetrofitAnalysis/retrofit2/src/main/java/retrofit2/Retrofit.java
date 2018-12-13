/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2;

import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Url;

import static java.util.Collections.unmodifiableList;
import static retrofit2.Utils.checkNotNull;

/**
 * Retrofit adapts a Java interface to HTTP calls by using annotations on the declared methods to
 * define how requests are made. Create instances using {@linkplain Builder
 * the builder} and pass your interface to {@link #create} to generate an implementation.
 * <p>
 * For example,
 * <pre><code>
 * Retrofit retrofit = new Retrofit.Builder()
 *     .baseUrl("https://api.example.com/")
 *     .addConverterFactory(GsonConverterFactory.create())
 *     .build();
 *
 * MyApi api = retrofit.create(MyApi.class);
 * Response&lt;User&gt; user = api.getUser().execute();
 * </code></pre>
 *
 * @author Bob Lee (bob@squareup.com)
 * @author Jake Wharton (jw@squareup.com)
 */
public final class Retrofit {

    private static final String TAG = "Retrofit";

    private final Map<Method, ServiceMethod<?, ?>> serviceMethodCache = new ConcurrentHashMap<>();

    // OkHttp
    final okhttp3.Call.Factory callFactory;
    final HttpUrl baseUrl;
    //
    final List<Converter.Factory> converterFactories;
    // CallAdapter适配器工厂集合
    final List<CallAdapter.Factory> callAdapterFactories;
    final @Nullable
    Executor callbackExecutor;
    // 是否在创建代理对象的时候就检测api接口中的方法是否正确
    final boolean validateEagerly;

    Retrofit(okhttp3.Call.Factory callFactory, HttpUrl baseUrl,
             List<Converter.Factory> converterFactories, List<CallAdapter.Factory> callAdapterFactories,
             @Nullable Executor callbackExecutor, boolean validateEagerly) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
        this.converterFactories = converterFactories; // Copy+unmodifiable at call site.
        this.callAdapterFactories = callAdapterFactories; // Copy+unmodifiable at call site.
        this.callbackExecutor = callbackExecutor;
        this.validateEagerly = validateEagerly;
    }

    /**
     * 通过创建一个代理对象来实现 API接口中的所有方法
     *
     * @param service API接口的Class对象
     * @param <T>     API接口的类型
     * @return 实现了API接口的所有方法的一个接口对象
     */
    @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
    public <T> T create(final Class<T> service) {
        // 1、判断 service是不是一个接口，同时这个接口不能继承其他接口
        Utils.validateServiceInterface(service);

        // 2、提前检测 api接口中的所有方法是否正确，然后将method封装成ServiceMethod并缓存到serviceMethodCache中
        if (validateEagerly) {
            eagerlyValidateMethods(service);
        }

        // 3、通过动态代理创建代理对象，调用service接口中的所有方法都会走到 invoke()方法中
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    private final Platform platform = Platform.get();

                    /**
                     * 当调用代理对象的任意一个方法都会走到这个方法当中
                     * @param proxy 调用该方法的对象（即那个接口对象）
                     * @param method 调用的方法
                     * @param args  方法的入参
                     * @return 返回这个调用方法执行后产生的返回值（如：Call<Result>、Observable）
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, @Nullable Object[] args)
                            throws Throwable {
                        // 1、如果该接口对象调用方法属于Object的，那么就直接执行然后返回
                        // getDeclaringClass()：该方法会返回这个method所在的类的Class，即这个method在哪一个父类或子类声明的
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        // platform.isDefaultMethod(method)：如果是Android则直接返回了false，如果是
                        // Java8则调用了isDefault()方法（isDefault()用于判断这个method是否为接口中的
                        // 使用default修饰的默认方法）
                        if (platform.isDefaultMethod(method)) {
                            return platform.invokeDefaultMethod(method, service, proxy, args);
                        }

                        // 2、将接口中中的Method封装成一个ServiceMethod，这个ServiceMethod可以在
                        // HTTP调用的时候直接使用
                        ServiceMethod<Object, Object> serviceMethod =
                                (ServiceMethod<Object, Object>) loadServiceMethod(method);

                        // 3、将ServiceMethod封装成一个OkHttpCall对象，OkHttpCall对象提供了请求
                        // 网络获取数据的功能
                        OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);

                        // 4、这里还使用 adapt()对 Call进行转换是为了可以让 Retrofit和RxJava进行
                        // 嵌套使用；如果用户没有设置 CallAdapter.Factory，那么就默认返回okHttpCall
                        return serviceMethod.adapt(okHttpCall);
                    }
                });
    }

    /**
     * 检测 api接口中的所有方法是否正确，检测完后会将生成的 ServiceMethod缓存在 serviceMethodCache中
     *
     * @param service api接口的Class
     */
    private void eagerlyValidateMethods(Class<?> service) {
        Platform platform = Platform.get();
        for (Method method : service.getDeclaredMethods()) {
            // platform.isDefaultMethod(method)直接返回了false
            // 可能会在 Retrofit后续的升级中做一些处理
            if (!platform.isDefaultMethod(method)) {
                // 将method加载到serviceMethodCache缓存中
                loadServiceMethod(method);
            }
        }
    }

    /**
     * 将method封装成ServiceMethod并缓存起来，最后return
     */
    ServiceMethod<?, ?> loadServiceMethod(Method method) {
        // 1、如果设置了属性 validateEagerly 为true，那么是可以在缓存中获取到这个ServiceMethod的，
        // 因为在前面已经加载了一遍了
        ServiceMethod<?, ?> result = serviceMethodCache.get(method);
        if (result != null) {
            return result;
        }

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                // 2、构建一个基本的 ServiceMethod对象
                result = new ServiceMethod.Builder<>(this, method).build();
                // 3、缓存起来
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    /**
     * The factory used to create {@linkplain okhttp3.Call OkHttp calls} for sending a HTTP requests.
     * Typically an instance of {@link OkHttpClient}.
     */
    public okhttp3.Call.Factory callFactory() {
        return callFactory;
    }

    /**
     * The API base URL.
     */
    public HttpUrl baseUrl() {
        return baseUrl;
    }

    /**
     * Returns a list of the factories tried when creating a
     * {@linkplain #callAdapter(Type, Annotation[])} call adapter}.
     */
    public List<CallAdapter.Factory> callAdapterFactories() {
        return callAdapterFactories;
    }

    /**
     * 从 Retrofit中的 CallAdapter工厂列表中选择合适的工厂，然后生产相应的 CallAdapter适配器；
     * 如果用户没有添加 创建CallAdapter的工厂，那么就会默认使用系统的 DefaultCallAdapterFactory
     * 工厂创建 CallAdapter适配器
     *
     * @param returnType  接口方法中的返回值类型
     * @param annotations 接口方法中的所有注解
     */
    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return nextCallAdapter(null, returnType, annotations);
    }

    /**
     * 遍历 callAdapterFactories（CallAdapter工厂集合，用户可以添加，但是这个工厂集合会在Retrofit
     * 构建的时候默认添加一个 DefaultCallAdapterFactory工厂），根据 returnType和 annotations让能对
     * 这两个数据进行处理的 CallAdapter工厂生产符合条件的 CallAdapter适配器，并返回
     *
     * @param skipPast 是否跳过指定的CallAdapter工厂
     */
    public CallAdapter<?, ?> nextCallAdapter(@Nullable CallAdapter.Factory skipPast, Type returnType,
                                             Annotation[] annotations) {
        // 1、检查接口方法的返回值类型是否为空
        checkNotNull(returnType, "returnType == null");
        // 2、检查接口方法是否有注解
        checkNotNull(annotations, "annotations == null");

        // 3、指定遍历所有CallAdapter工厂的起点索引，如果skipPast为空，indexOf就返回-1,
        int start = callAdapterFactories.indexOf(skipPast) + 1;

        // 4、从指定位置开始遍历所有的，一般情况下都是从0开始
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            // 5、根据这个 returnType和 annotations 获取符合条件的CallAdapter适配器
            CallAdapter<?, ?> adapter = callAdapterFactories.get(i).get(returnType, annotations, this);
            if (adapter != null) {
                return adapter;
            }
        }

        // 6、如果没有获取到符合条件的 CallAdapter适配器
        StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
                .append(returnType)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns an unmodifiable list of the factories tried when creating a
     * {@linkplain #requestBodyConverter(Type, Annotation[], Annotation[]) request body converter}, a
     * {@linkplain #responseBodyConverter(Type, Annotation[]) response body converter}, or a
     * {@linkplain #stringConverter(Type, Annotation[]) string converter}.
     */
    public List<Converter.Factory> converterFactories() {
        return converterFactories;
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
     * {@linkplain #converterFactories() factories}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<T, RequestBody> requestBodyConverter(Type type,
                                                              Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        return nextRequestBodyConverter(null, type, parameterAnnotations, methodAnnotations);
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
     * {@linkplain #converterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<T, RequestBody> nextRequestBodyConverter(
            @Nullable Converter.Factory skipPast, Type type, Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations) {
        checkNotNull(type, "type == null");
        checkNotNull(parameterAnnotations, "parameterAnnotations == null");
        checkNotNull(methodAnnotations, "methodAnnotations == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            Converter.Factory factory = converterFactories.get(i);
            Converter<?, RequestBody> converter =
                    factory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<T, RequestBody>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ")
                .append(type)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * 获取能将 OkHttp请求数据后返回的ResponseBody 转换成 responseType类型（如：UserInfo）所
     * 对应的对象的一个转换器
     *
     * @param type        这是 ResponseBody要转换成的目标对象的类型
     * @param annotations 这是api接口方法上标注的所有注解
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
        return nextResponseBodyConverter(null, type, annotations);
    }

    /**
     * 获取能将 OkHttp请求数据后返回的ResponseBody 转换成 responseType类型（如：UserInfo）所
     * 对应的对象的一个转换器
     *
     * @param skipPast    在这个Converter.Factory工厂之后进行查找
     * @param type        这是 ResponseBody要转换成的目标对象的类型
     * @param annotations 这是api接口方法上标注的所有注解
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<ResponseBody, T> nextResponseBodyConverter(
            @Nullable Converter.Factory skipPast, Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            // 如果设置了 GsonConverterFactory，那么会调用到其 responseBodyConverter()获取到一
            // 个 GsonResponseBodyConverter 转换器，这个转换器能将 ResponseBody 转换成 type指定
            // 的对象类型
            Converter<ResponseBody, ?> converter =
                    converterFactories.get(i).responseBodyConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<ResponseBody, T>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
                .append(type)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * 返回一个能转换成String的Converter工厂
     * {@linkplain #converterFactories() factories}.
     */
    public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        // 遍历所有的转换工厂（converterFactories），看是否有工厂实现了 stringConverter()转换方法
        for (int i = 0, count = converterFactories.size(); i < count; i++) {
            Converter<?, String> converter =
                    converterFactories.get(i).stringConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<T, String>) converter;
            }
        }

        // Nothing matched. Resort to default converter which just calls toString().
        //noinspection unchecked
        // 返回默认的的 Converter
        return (Converter<T, String>) BuiltInConverters.ToStringConverter.INSTANCE;
    }

    /**
     * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
     * in which case callbacks should be made synchronously on the background thread.
     */
    public @Nullable
    Executor callbackExecutor() {
        return callbackExecutor;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Build a new {@link Retrofit}.
     * <p>
     * Calling {@link #baseUrl} is required before calling {@link #build()}. All other methods
     * are optional.
     */
    public static final class Builder {
        // 这个 Platform在创建 Builder的时候就被实例化了
        private final Platform platform;
        private @Nullable
        okhttp3.Call.Factory callFactory;
        private HttpUrl baseUrl;
        private final List<Converter.Factory> converterFactories = new ArrayList<>();
        private final List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        private @Nullable
        Executor callbackExecutor;
        private boolean validateEagerly;

        Builder(Platform platform) {
            this.platform = platform;
        }

        // 这个构造函数会被用户使用
        public Builder() {
            this(Platform.get());
        }

        Builder(Retrofit retrofit) {
            platform = Platform.get();
            callFactory = retrofit.callFactory;
            baseUrl = retrofit.baseUrl;

            converterFactories.addAll(retrofit.converterFactories);
            // Remove the default BuiltInConverters instance added by build().
            converterFactories.remove(0);

            callAdapterFactories.addAll(retrofit.callAdapterFactories);
            // Remove the default, platform-aware call adapter added by build().
            callAdapterFactories.remove(callAdapterFactories.size() - 1);

            callbackExecutor = retrofit.callbackExecutor;
            validateEagerly = retrofit.validateEagerly;
        }

        /**
         * 设置网络请求工厂，用户可以设置一个 进行了改良的OkHttp
         * <p>
         * This is a convenience method for calling {@link #callFactory}.
         */
        public Builder client(OkHttpClient client) {
            return callFactory(checkNotNull(client, "client == null"));
        }

        /**
         * Specify a custom call factory for creating {@link Call} instances.
         * <p>
         * Note: Calling {@link #client} automatically sets this value.
         */
        public Builder callFactory(okhttp3.Call.Factory factory) {
            this.callFactory = checkNotNull(factory, "factory == null");
            return this;
        }

        /**
         * 设置最基本的 url
         *
         * @see #baseUrl(HttpUrl)
         */
        public Builder baseUrl(String baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            // 将 baseUrl解析成一个 HttpUrl，然后对这个HttpUrl进行检测
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }

            // 调用 baseUrl()方法对 httpUrl进行检测，看是否存在问题
            return baseUrl(httpUrl);
        }


        public Builder baseUrl(HttpUrl baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * 添加一个 Converter转换器工厂，用于将后台返回的Response数据转换成指定的对象
         * Add converter factory for serialization and deserialization of objects.
         */
        public Builder addConverterFactory(Converter.Factory factory) {
            converterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        /**
         * Add a call adapter factory for supporting service method return types other than {@link
         * Call}.
         */
        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            callAdapterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        /**
         * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
         * your service method.
         * <p>
         * Note: {@code executor} is not used for {@linkplain #addCallAdapterFactory custom method
         * return types}.
         */
        public Builder callbackExecutor(Executor executor) {
            this.callbackExecutor = checkNotNull(executor, "executor == null");
            return this;
        }

        /**
         * Returns a modifiable list of call adapter factories.
         */
        public List<CallAdapter.Factory> callAdapterFactories() {
            return this.callAdapterFactories;
        }

        /**
         * Returns a modifiable list of converter factories.
         */
        public List<Converter.Factory> converterFactories() {
            return this.converterFactories;
        }

        /**
         * 如果设置为true，那么就会在调用 retrofit.create()方法创建 api接口对象的时候，就会在这个
         * create()方法中遍历 api接口中的所有方法是否正确，同时会将这些方法生成的 ServiceMethod
         * 保存在 serviceMethodCache 缓存中
         */
        public Builder validateEagerly(boolean validateEagerly) {
            this.validateEagerly = validateEagerly;
            return this;
        }

        /**
         * Create the {@link Retrofit} instance using the configured values.
         * <p>
         * Note: If neither {@link #client} nor {@link #callFactory} is called a default {@link
         * OkHttpClient} will be created and used.
         */
        public Retrofit build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            // 1、如果用户没有设置 OkHttpClient，那么就创建一个默认的 OkHttpClient
            okhttp3.Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            // 2、如果用户没有设置 callbackExecutor，那么就获取本平台（Android）默认的MainThreadExecutor
            // 这个callbackExecutor是什么：只是一个实现了 Executor接口的使用Handler让逻辑在主线
            // 程中执行的一个对象
            Executor callbackExecutor = this.callbackExecutor;
            if (callbackExecutor == null) {
                callbackExecutor = platform.defaultCallbackExecutor();
            }

            //<-----------   3、下面开始添加 CallAdapter.Factory 工厂  ---------->

            // 获取用户设置的所有 CallAdapter.Factory 工厂
            List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
            // 添加 Android平台默认的 CallAdapter.Factory 工厂（即：ExecutorCallAdapterFactory）
            callAdapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));


            //<-----------  4、下面开始添加 Converter.Factory 工厂  ---------->

            // 根据用户设置的转换器工厂的数量，然后加一，去构建ArrayList集合
            // 之所以加一是因为：后面会添加一个系统的 BuiltInConverters转换器工厂
            List<Converter.Factory> converterFactories =
                    new ArrayList<>(1 + this.converterFactories.size());

            // 添加 Retrofit自带的 Converter转换器工厂 BuiltInConverters，确保即使用户没有设置 Converter
            // 转换器工厂，Retrofit也能正常工作
            converterFactories.add(new BuiltInConverters());
            // 添加用户设置的所有的 Converter.Factory 工厂
            converterFactories.addAll(this.converterFactories);

            // 5、最后创建一个 Retrofit对象
            // Collections.unmodifiableList()方法会将 converterFactories这个 ArrayList转换成一个
            // UnmodifiableRandomAccessList，表示这个ArrayList不能被修改，这就保证了这个 ArrayList
            // 传入 Retrofit中后便不能被修改
            return new Retrofit(callFactory, baseUrl, unmodifiableList(converterFactories),
                    unmodifiableList(callAdapterFactories), callbackExecutor, validateEagerly);
        }
    }

}
