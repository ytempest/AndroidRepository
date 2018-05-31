/*
 * Copyright (C) 2015 Square, Inc.
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.OPTIONS;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.QueryName;
import retrofit2.http.Url;

/**
 * Description：将接口中的方法适配成HTTP调用的时候使用的ServiceMethod；
 * Method --> ServiceMethod
 * <p>
 * 一个 ServiceMethod封装了一个 url请求需要的 基本的url、数据转换器、HTTP请求方式、头部信息、
 * contentType、url的key集合（parameterHandlers）等
 * <p>
 * ServiceMethod类中的 toCall()方法会调用 网络工厂（callFactory）将ServiceMethod对象转换成一个
 * Call对象，这个Call对象会被用于进行网络请求
 *
 * @param <R> 这个泛型指定 Retrofit中的 Call<R>，也就是 Result Bean
 * @param <T> 这个泛型指定的是 api接口方法的返回值类型
 */
final class ServiceMethod<R, T> {

    private static final String TAG = "ServiceMethod";

    // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
    static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
    static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);

    // 网络请求工厂，默认使用的是 OkHttp
    private final okhttp3.Call.Factory callFactory;
    // CallAdapter适配器，用于将Call适配成其他对象（如RxJava的Observable）
    private final CallAdapter<R, T> callAdapter;

    private final HttpUrl baseUrl;
    // 对后台返回的响应体进行转换的转换器，只有一个，也就是说只会使用
    private final Converter<ResponseBody, R> responseConverter;
    // HTTP请求方式
    private final String httpMethod;
    // 相对url地址
    private final String relativeUrl;
    private final Headers headers;
    private final MediaType contentType;
    // 是否有报文Body，PATCH、POST、PUT等网络请求是有的
    private final boolean hasBody;
    private final boolean isFormEncoded;
    private final boolean isMultipart;
    // 参数处理对象的数组
    private final ParameterHandler<?>[] parameterHandlers;

    ServiceMethod(Builder<R, T> builder) {
        this.callFactory = builder.retrofit.callFactory();
        this.callAdapter = builder.callAdapter;
        this.baseUrl = builder.retrofit.baseUrl();
        this.responseConverter = builder.responseConverter;
        this.httpMethod = builder.httpMethod;
        this.relativeUrl = builder.relativeUrl;
        this.headers = builder.headers;
        this.contentType = builder.contentType;
        this.hasBody = builder.hasBody;
        this.isFormEncoded = builder.isFormEncoded;
        this.isMultipart = builder.isMultipart;
        this.parameterHandlers = builder.parameterHandlers;
    }

    /**
     * 将ServiceMethod中保存的数据和所有的args（即url中的参数的值）用于构建一个 OkHttp可以使用
     * 的 Call请求
     * Builds an HTTP request from method arguments.
     */
    okhttp3.Call toCall(@Nullable Object... args) throws IOException {

        // 1、构建一个 Retrofit的 Request
        RequestBuilder requestBuilder = new RequestBuilder(httpMethod, baseUrl, relativeUrl, headers,
                contentType, hasBody, isFormEncoded, isMultipart);

        // It is an error to invoke a method with the wrong arg types.
        @SuppressWarnings("unchecked")
        ParameterHandler<Object>[] handlers = (ParameterHandler<Object>[]) parameterHandlers;

        // 2、检测：如果接口方法的参数的 ParameterHandler数量和 方法的传入的参数数量不一致就报错
        int argumentCount = args != null ? args.length : 0;
        if (argumentCount != handlers.length) {
            throw new IllegalArgumentException("Argument count (" + argumentCount
                    + ") doesn't match expected count (" + handlers.length + ")");
        }

        // 3、遍历接口方法的所有参数，然后调用 apply()方法将参数值转换成指定的值，然后添加到
        // requestBuilder中
        for (int p = 0; p < argumentCount; p++) {
            handlers[p].apply(requestBuilder, args[p]);
        }

        // 4 、最后使用OkHttp将 Request 封装成一个 Call
        return callFactory.newCall(requestBuilder.build());
    }

    /**
     * 这个方法会调用 CallAdapter 类中的adapt()方法将 Call对象适配成指定的 T类型的对象；
     * Retrofit之所以能和 RxJava进行嵌套使用就是因为这个方法，只要为 Retrofit添加一个能将 Retrofit
     * 的 Call<> 对象转换成 RxJava的 Observable对象的一个 CallAdapter适配器，就能实现
     */
    T adapt(Call<R> call) {
        return callAdapter.adapt(call);
    }

    /**
     * 调用 ResponseConverter转换器，将后台返回的ResponseBody转换成相应的 Result Bean
     */
    R toResponse(ResponseBody body) throws IOException {
        return responseConverter.convert(body);
    }

    /**
     * Inspects the annotations on an interface method to construct a reusable service method. This
     * requires potentially-expensive reflection so it is best to build each service method only once
     * and reuse it. Builders cannot be reused.
     */
    static final class Builder<T, R> {
        final Retrofit retrofit;
        final Method method;
        final Annotation[] methodAnnotations;
        // 接口方法中的所有参数的注解
        final Annotation[][] parameterAnnotationsArray;
        // 接口方法中的参数类型
        final Type[] parameterTypes;

        // 接口方法的返回值类型是 Call<UserInfoResult>，那么这个 responseType 就是UserInfoResult
        Type responseType;
        boolean gotField;
        boolean gotPart;
        boolean gotBody;
        boolean gotPath;
        boolean gotQuery;
        boolean gotUrl;
        String httpMethod;
        boolean hasBody;
        boolean isFormEncoded;
        boolean isMultipart;
        String relativeUrl;
        Headers headers;
        MediaType contentType;
        Set<String> relativeUrlParamNames;
        ParameterHandler<?>[] parameterHandlers;
        Converter<ResponseBody, T> responseConverter;
        CallAdapter<T, R> callAdapter;

        Builder(Retrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            // 1、获取方法上标记的所有注解（返回：Annotation[]）
            this.methodAnnotations = method.getAnnotations();
            // 2、获取接口方法中的所有参数的类型（返回：Type[]）
            this.parameterTypes = method.getGenericParameterTypes();
            // 3、获取接口方法中的所有参数的注解（返回：Annotation[][]）
            this.parameterAnnotationsArray = method.getParameterAnnotations();
        }


        public ServiceMethod build() {
            // 1、创建 CallAdapter适配器
            callAdapter = createCallAdapter();

            // 2、通过调用callAdapter的 responseType()方法获取响应体类型，如接口方法的返回值
            // 类型是 Call<UserInfoResult>，那么这个 responseType 就是UserInfoResult
            responseType = callAdapter.responseType();

            // 3、检测 responseType不能是 Retrofit的Response 或者 OkHttp的Response，否则抛异常
            if (responseType == Response.class || responseType == okhttp3.Response.class) {
                throw methodError("'"
                        + Utils.getRawType(responseType).getName()
                        + "' is not a valid response body type. Did you mean ResponseBody?");
            }

            // 4、 通过 createResponseConverter()方法获取响应转换器，也就是获取能将 OkHttp请求数据
            // 成功后返回的 ResponseBody 装换成 UserInfoResult 对象的一个转换器（如果设置了
            // GsonConverterFactory工厂，那么就会获取到一个 GsonResponseBodyConverter转换器）
            responseConverter = createResponseConverter();

            // 5、遍历接口方法上的所有注解，解析接口方法上的注解，然后根据其类型进行处理
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            // 6、如果 httpMethod为空，就表明api接口方法没有设置网络请求方式的注解 或者 设置有错
            if (httpMethod == null) {
                throw methodError("HTTP method annotation is required (e.g., @GET, @POST, etc.).");
            }

            // 7、检测是否有设置报文有body（即没有使用PATCH、POST、PUT），但是又使用了
            // Multipart注解和 FormUrlEncoded注解，如果有就抛异常
            if (!hasBody) {
                if (isMultipart) {
                    throw methodError(
                            "Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
                }
                if (isFormEncoded) {
                    throw methodError("FormUrlEncoded can only be specified on HTTP methods with "
                            + "request body (e.g., @POST).");
                }
            }

            int parameterCount = parameterAnnotationsArray.length;
            parameterHandlers = new ParameterHandler<?>[parameterCount];

            // 8、遍历api接口方法中的所有参数类型，并使用 ParameterHandler封装每一个参数类型
            for (int p = 0; p < parameterCount; p++) {

                // 8.1、检测参数类型是否存在通配符（即如：Data<?>），如果有就报异常
                Type parameterType = parameterTypes[p];
                if (Utils.hasUnresolvableType(parameterType)) {
                    throw parameterError(p, "Parameter type must not include a type variable or wildcard: %s",
                            parameterType);
                }

                // 8.2、获取api接口方法中 第p个位置的参数的注解，检测该参数是否使用了注解，如果
                // 没有使用就抛异常
                Annotation[] parameterAnnotations = parameterAnnotationsArray[p];
                if (parameterAnnotations == null) {
                    throw parameterError(p, "No Retrofit annotation found.");
                }

                // 8.3、解析接口方法的注解，并将每一个接口方法的参数（类型和注解）封装成一个
                // ParameterHandler对象
                parameterHandlers[p] = parseParameter(p, parameterType, parameterAnnotations);
            }

            // 9、做一些检测工作
            if (relativeUrl == null && !gotUrl) {
                throw methodError("Missing either @%s URL or @Url parameter.", httpMethod);
            }
            if (!isFormEncoded && !isMultipart && !hasBody && gotBody) {
                throw methodError("Non-body HTTP method cannot contain @Body.");
            }
            if (isFormEncoded && !gotField) {
                throw methodError("Form-encoded method must contain at least one @Field.");
            }
            if (isMultipart && !gotPart) {
                throw methodError("Multipart method must contain at least one @Part.");
            }

            // 10、最后创建一个 ServiceMethod
            return new ServiceMethod<>(this);
        }

        /**
         * 根据 接口方法的返回值类型和接口方法的注解获取相应的 CallAdapter适配器
         * <p>
         * 如果用户没有添加 CallAdapter工厂，那么就会默认使用系统的 DefaultCallAdapterFactory
         * 工厂生产 CallAdapter适配器
         */
        private CallAdapter<T, R> createCallAdapter() {
            // 1、获取自定义接口方法中的返回值类型，下面会对这个返回值做一些判断
            Type returnType = method.getGenericReturnType();

            // 2、检测返回值类型是否正确，如：Call<?>是不合法的，不能使用 ？通配符
            if (Utils.hasUnresolvableType(returnType)) {
                throw methodError(
                        "Method return type must not include a type variable or wildcard: %s", returnType);
            }

            // 3、如果接口方法返回值是void就抛异常
            if (returnType == void.class) {
                throw methodError("Service methods cannot return void.");
            }

            // 4、获取接口方法上标注的所有注解
            Annotation[] annotations = method.getAnnotations();
            try {
                // 根据 returnType 和 annotations 获取能处理这两个数据的 CallAdapter适配器
                // noinspection unchecked
                return (CallAdapter<T, R>) retrofit.callAdapter(returnType, annotations);
            } catch (RuntimeException e) {
                // Wide exception range because factories are user code.
                throw methodError(e, "Unable to create call adapter for %s", returnType);
            }
        }

        private void parseMethodAnnotation(Annotation annotation) {
            // 1、下面的代码检测网络请求的方式
            if (annotation instanceof DELETE) {
                parseHttpMethodAndPath("DELETE", ((DELETE) annotation).value(), false);

            } else if (annotation instanceof GET) {
                parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);

            } else if (annotation instanceof HEAD) {
                parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
                if (!Void.class.equals(responseType)) {
                    throw methodError("HEAD method must use Void as response type.");
                }

            } else if (annotation instanceof PATCH) {
                // 如果是 PATCH，那么报文是有 Body的，所以设置 hasBody为true
                parseHttpMethodAndPath("PATCH", ((PATCH) annotation).value(), true);

            } else if (annotation instanceof POST) {
                // 如果是 POST，那么报文是有 Body的，所以设置 hasBody为true
                parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);

            } else if (annotation instanceof PUT) {
                // 如果是 PUT，那么报文是有 Body的，所以设置 hasBody为true
                parseHttpMethodAndPath("PUT", ((PUT) annotation).value(), true);

            } else if (annotation instanceof OPTIONS) {
                parseHttpMethodAndPath("OPTIONS", ((OPTIONS) annotation).value(), false);

            } else if (annotation instanceof HTTP) {
                HTTP http = (HTTP) annotation;
                parseHttpMethodAndPath(http.method(), http.path(), http.hasBody());


                // 这里开始处理头信息
            } else if (annotation instanceof retrofit2.http.Headers) {
                // 获取所有头信息注解的所有值
                String[] headersToParse = ((retrofit2.http.Headers) annotation).value();
                if (headersToParse.length == 0) {
                    throw methodError("@Headers annotation is empty.");
                }

                // 解析所有的头信息
                headers = parseHeaders(headersToParse);


                // Multipart注解 和 FormUrlEncoded注解不能同时使用，只能二选一
            } else if (annotation instanceof Multipart) {
                if (isFormEncoded) {
                    throw methodError("Only one encoding annotation is allowed.");
                }
                isMultipart = true;

            } else if (annotation instanceof FormUrlEncoded) {
                if (isMultipart) {
                    throw methodError("Only one encoding annotation is allowed.");
                }
                isFormEncoded = true;
            }
        }

        /**
         * @param httpMethod 请求方式，GET、POST等
         * @param value      注解的值，有些注解设置了默认的值，如：GET
         * @param hasBody    是否有 RequestBody
         */
        private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
            if (this.httpMethod != null) {
                throw methodError("Only one HTTP method is allowed. Found: %s and %s.",
                        this.httpMethod, httpMethod);
            }
            this.httpMethod = httpMethod;
            this.hasBody = hasBody;

            // 如果没有注解值就return
            if (value.isEmpty()) {
                return;
            }

            // Get the relative URL path and existing query string, if present.
            int question = value.indexOf('?');
            if (question != -1 && question < value.length() - 1) {
                // Ensure the query string does not have any named parameters.
                String queryParams = value.substring(question + 1);
                Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
                if (queryParamMatcher.find()) {
                    throw methodError("URL query string \"%s\" must not have replace block. "
                            + "For dynamic query parameters use @Query.", queryParams);
                }
            }

            this.relativeUrl = value;
            this.relativeUrlParamNames = parsePathParameters(value);
        }

        /**
         * 解析所有的头信息，并将这些信息封装到 Headers对象中
         *
         * @param headers 头信息数组，如：{"Content-Type:text/plain", "Connection:keep-alive"}
         */
        private Headers parseHeaders(String[] headers) {
            Headers.Builder builder = new Headers.Builder();
            // 遍历添加所有的头信息到 builder中
            for (String header : headers) {
                int colon = header.indexOf(':');
                if (colon == -1 || colon == 0 || colon == header.length() - 1) {
                    throw methodError(
                            "@Headers value must be in the form \"Name: Value\". Found: \"%s\"", header);
                }
                String headerName = header.substring(0, colon);
                String headerValue = header.substring(colon + 1).trim();

                // 如果是 Content-Type 头信息就将该头信息的值保存在 contentType属性中
                if ("Content-Type".equalsIgnoreCase(headerName)) {
                    MediaType type = MediaType.parse(headerValue);
                    if (type == null) {
                        throw methodError("Malformed content type: %s", headerValue);
                    }
                    contentType = type;
                } else {
                    builder.add(headerName, headerValue);
                }
            }
            return builder.build();
        }

        /**
         * 封装方法参数（参数类型、标记在参数上的注解）到 ParameterHandler 中
         * 一个方法参数对应一个 ParameterHandler 对象
         *
         * @param p             参数在方法中所处的位置
         * @param parameterType 参数类型的Type
         * @param annotations   参数的注解
         */
        private ParameterHandler<?> parseParameter(
                int p, Type parameterType, Annotation[] annotations) {
            ParameterHandler<?> result = null;
            for (Annotation annotation : annotations) {
                // 将接口方法的第p个参数封装成一个 ParameterHandler
                ParameterHandler<?> annotationAction = parseParameterAnnotation(
                        p, parameterType, annotations, annotation);

                if (annotationAction == null) {
                    continue;
                }

                if (result != null) {
                    throw parameterError(p, "Multiple Retrofit annotations found, only one allowed.");
                }

                result = annotationAction;
            }

            if (result == null) {
                throw parameterError(p, "No Retrofit annotation found.");
            }

            return result;
        }

        /**
         * 将接口方法中的参数类型和参数注解封装成一个 ParameterHandler
         *
         * @param p           参数注解在接口方法中的位置
         * @param type        接口方法参数的类型
         * @param annotations 第p个位置的接口方法参数的所有注解组成的数组
         * @param annotation  第p个位置的接口方法参数的注解，已知Retrofit提供的接口方法的参数注解
         *                    有：Url、Path、Query、QueryName、QueryMap、Header、HeaderMap、Field、FieldMap
         *                    、Part、PartMap、Body这些
         */
        private ParameterHandler<?> parseParameterAnnotation(
                int p, Type type, Annotation[] annotations, Annotation annotation) {
            if (annotation instanceof Url) {
                if (gotUrl) {
                    throw parameterError(p, "Multiple @Url method annotations found.");
                }
                if (gotPath) {
                    throw parameterError(p, "@Path parameters may not be used with @Url.");
                }
                if (gotQuery) {
                    throw parameterError(p, "A @Url parameter must not come after a @Query");
                }
                if (relativeUrl != null) {
                    throw parameterError(p, "@Url cannot be used with @%s URL", httpMethod);
                }

                gotUrl = true;

                // 如果使用 Url注解，同时参数类型是HttpUrl 或 String 或 URI 或 android.net.Uri，就
                // 返回一个 RelativeUrl（继承自ParameterHandler）对象
                if (type == HttpUrl.class
                        || type == String.class
                        || type == URI.class
                        || (type instanceof Class && "android.net.Uri".equals(((Class<?>) type).getName()))) {
                    return new ParameterHandler.RelativeUrl();
                } else {
                    throw parameterError(p,
                            "@Url must be okhttp3.HttpUrl, String, java.net.URI, or android.net.Uri type.");
                }

            } else if (annotation instanceof Path) {
                if (gotQuery) {
                    throw parameterError(p, "A @Path parameter must not come after a @Query.");
                }
                if (gotUrl) {
                    throw parameterError(p, "@Path parameters may not be used with @Url.");
                }
                if (relativeUrl == null) {
                    throw parameterError(p, "@Path can only be used with relative url on @%s", httpMethod);
                }
                gotPath = true;

                Path path = (Path) annotation;
                String name = path.value();
                validatePathName(p, name);

                Converter<?, String> converter = retrofit.stringConverter(type, annotations);
                return new ParameterHandler.Path<>(name, converter, path.encoded());

            } else if (annotation instanceof Query) {
                Query query = (Query) annotation;
                String name = query.value();
                boolean encoded = query.encoded();

                Class<?> rawParameterType = Utils.getRawType(type);
                gotQuery = true;
                if (Iterable.class.isAssignableFrom(rawParameterType)) {
                    if (!(type instanceof ParameterizedType)) {
                        throw parameterError(p, rawParameterType.getSimpleName()
                                + " must include generic type (e.g., "
                                + rawParameterType.getSimpleName()
                                + "<String>)");
                    }
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                    Converter<?, String> converter =
                            retrofit.stringConverter(iterableType, annotations);
                    return new ParameterHandler.Query<>(name, converter, encoded).iterable();
                } else if (rawParameterType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                    Converter<?, String> converter =
                            retrofit.stringConverter(arrayComponentType, annotations);
                    return new ParameterHandler.Query<>(name, converter, encoded).array();
                } else {
                    Converter<?, String> converter =
                            retrofit.stringConverter(type, annotations);
                    return new ParameterHandler.Query<>(name, converter, encoded);
                }

            } else if (annotation instanceof QueryName) {
                QueryName query = (QueryName) annotation;
                boolean encoded = query.encoded();

                Class<?> rawParameterType = Utils.getRawType(type);
                gotQuery = true;
                if (Iterable.class.isAssignableFrom(rawParameterType)) {
                    if (!(type instanceof ParameterizedType)) {
                        throw parameterError(p, rawParameterType.getSimpleName()
                                + " must include generic type (e.g., "
                                + rawParameterType.getSimpleName()
                                + "<String>)");
                    }
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                    Converter<?, String> converter =
                            retrofit.stringConverter(iterableType, annotations);
                    return new ParameterHandler.QueryName<>(converter, encoded).iterable();
                } else if (rawParameterType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                    Converter<?, String> converter =
                            retrofit.stringConverter(arrayComponentType, annotations);
                    return new ParameterHandler.QueryName<>(converter, encoded).array();
                } else {
                    Converter<?, String> converter =
                            retrofit.stringConverter(type, annotations);
                    return new ParameterHandler.QueryName<>(converter, encoded);
                }

            } else if (annotation instanceof QueryMap) {
                Class<?> rawParameterType = Utils.getRawType(type);
                if (!Map.class.isAssignableFrom(rawParameterType)) {
                    throw parameterError(p, "@QueryMap parameter type must be Map.");
                }
                Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
                if (!(mapType instanceof ParameterizedType)) {
                    throw parameterError(p, "Map must include generic types (e.g., Map<String, String>)");
                }
                ParameterizedType parameterizedType = (ParameterizedType) mapType;
                Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
                if (String.class != keyType) {
                    throw parameterError(p, "@QueryMap keys must be of type String: " + keyType);
                }
                Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
                Converter<?, String> valueConverter =
                        retrofit.stringConverter(valueType, annotations);

                return new ParameterHandler.QueryMap<>(valueConverter, ((QueryMap) annotation).encoded());

            } else if (annotation instanceof Header) {
                Header header = (Header) annotation;
                String name = header.value();

                Class<?> rawParameterType = Utils.getRawType(type);
                if (Iterable.class.isAssignableFrom(rawParameterType)) {
                    if (!(type instanceof ParameterizedType)) {
                        throw parameterError(p, rawParameterType.getSimpleName()
                                + " must include generic type (e.g., "
                                + rawParameterType.getSimpleName()
                                + "<String>)");
                    }
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                    Converter<?, String> converter =
                            retrofit.stringConverter(iterableType, annotations);
                    return new ParameterHandler.Header<>(name, converter).iterable();
                } else if (rawParameterType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                    Converter<?, String> converter =
                            retrofit.stringConverter(arrayComponentType, annotations);
                    return new ParameterHandler.Header<>(name, converter).array();
                } else {
                    Converter<?, String> converter =
                            retrofit.stringConverter(type, annotations);
                    return new ParameterHandler.Header<>(name, converter);
                }

            } else if (annotation instanceof HeaderMap) {
                Class<?> rawParameterType = Utils.getRawType(type);
                if (!Map.class.isAssignableFrom(rawParameterType)) {
                    throw parameterError(p, "@HeaderMap parameter type must be Map.");
                }
                Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
                if (!(mapType instanceof ParameterizedType)) {
                    throw parameterError(p, "Map must include generic types (e.g., Map<String, String>)");
                }
                ParameterizedType parameterizedType = (ParameterizedType) mapType;
                Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
                if (String.class != keyType) {
                    throw parameterError(p, "@HeaderMap keys must be of type String: " + keyType);
                }
                Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
                Converter<?, String> valueConverter =
                        retrofit.stringConverter(valueType, annotations);

                return new ParameterHandler.HeaderMap<>(valueConverter);

            } else if (annotation instanceof Field) {
                // 如果使用了 Field注解，就要和 FormUrlEncoded注解一起使用
                if (!isFormEncoded) {
                    throw parameterError(p, "@Field parameters can only be used with form encoding.");
                }

                Field field = (Field) annotation;
                String name = field.value();
                boolean encoded = field.encoded();

                gotField = true;

                // 对参数类型进行一些处理
                Class<?> rawParameterType = Utils.getRawType(type);
                if (Iterable.class.isAssignableFrom(rawParameterType)) {
                    if (!(type instanceof ParameterizedType)) {
                        throw parameterError(p, rawParameterType.getSimpleName()
                                + " must include generic type (e.g., "
                                + rawParameterType.getSimpleName()
                                + "<String>)");
                    }
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                    Converter<?, String> converter =
                            retrofit.stringConverter(iterableType, annotations);
                    return new ParameterHandler.Field<>(name, converter, encoded).iterable();
                } else if (rawParameterType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                    Converter<?, String> converter =
                            retrofit.stringConverter(arrayComponentType, annotations);
                    return new ParameterHandler.Field<>(name, converter, encoded).array();
                } else {
                    // 一般都是走到这里

                    // 获取Converter转换工厂
                    Converter<?, String> converter = retrofit.stringConverter(type, annotations);
                    // 因为这里解析的是 @Field注解，所以返回一个 ParameterHandler.Field对象
                    return new ParameterHandler.Field<>(name, converter, encoded);
                }

            } else if (annotation instanceof FieldMap) {
                if (!isFormEncoded) {
                    throw parameterError(p, "@FieldMap parameters can only be used with form encoding.");
                }
                Class<?> rawParameterType = Utils.getRawType(type);
                if (!Map.class.isAssignableFrom(rawParameterType)) {
                    throw parameterError(p, "@FieldMap parameter type must be Map.");
                }
                Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
                if (!(mapType instanceof ParameterizedType)) {
                    throw parameterError(p,
                            "Map must include generic types (e.g., Map<String, String>)");
                }
                ParameterizedType parameterizedType = (ParameterizedType) mapType;
                Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
                if (String.class != keyType) {
                    throw parameterError(p, "@FieldMap keys must be of type String: " + keyType);
                }
                Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
                Converter<?, String> valueConverter =
                        retrofit.stringConverter(valueType, annotations);

                gotField = true;
                return new ParameterHandler.FieldMap<>(valueConverter, ((FieldMap) annotation).encoded());

            } else if (annotation instanceof Part) {
                if (!isMultipart) {
                    throw parameterError(p, "@Part parameters can only be used with multipart encoding.");
                }
                Part part = (Part) annotation;
                gotPart = true;

                String partName = part.value();
                Class<?> rawParameterType = Utils.getRawType(type);
                if (partName.isEmpty()) {
                    if (Iterable.class.isAssignableFrom(rawParameterType)) {
                        if (!(type instanceof ParameterizedType)) {
                            throw parameterError(p, rawParameterType.getSimpleName()
                                    + " must include generic type (e.g., "
                                    + rawParameterType.getSimpleName()
                                    + "<String>)");
                        }
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                        if (!MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(iterableType))) {
                            throw parameterError(p,
                                    "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
                        }
                        return ParameterHandler.RawPart.INSTANCE.iterable();
                    } else if (rawParameterType.isArray()) {
                        Class<?> arrayComponentType = rawParameterType.getComponentType();
                        if (!MultipartBody.Part.class.isAssignableFrom(arrayComponentType)) {
                            throw parameterError(p,
                                    "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
                        }
                        return ParameterHandler.RawPart.INSTANCE.array();
                    } else if (MultipartBody.Part.class.isAssignableFrom(rawParameterType)) {
                        return ParameterHandler.RawPart.INSTANCE;
                    } else {
                        throw parameterError(p,
                                "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
                    }
                } else {
                    Headers headers =
                            Headers.of("Content-Disposition", "form-data; name=\"" + partName + "\"",
                                    "Content-Transfer-Encoding", part.encoding());

                    if (Iterable.class.isAssignableFrom(rawParameterType)) {
                        if (!(type instanceof ParameterizedType)) {
                            throw parameterError(p, rawParameterType.getSimpleName()
                                    + " must include generic type (e.g., "
                                    + rawParameterType.getSimpleName()
                                    + "<String>)");
                        }
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                        if (MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(iterableType))) {
                            throw parameterError(p, "@Part parameters using the MultipartBody.Part must not "
                                    + "include a part name in the annotation.");
                        }
                        Converter<?, RequestBody> converter =
                                retrofit.requestBodyConverter(iterableType, annotations, methodAnnotations);
                        return new ParameterHandler.Part<>(headers, converter).iterable();
                    } else if (rawParameterType.isArray()) {
                        Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                        if (MultipartBody.Part.class.isAssignableFrom(arrayComponentType)) {
                            throw parameterError(p, "@Part parameters using the MultipartBody.Part must not "
                                    + "include a part name in the annotation.");
                        }
                        Converter<?, RequestBody> converter =
                                retrofit.requestBodyConverter(arrayComponentType, annotations, methodAnnotations);
                        return new ParameterHandler.Part<>(headers, converter).array();
                    } else if (MultipartBody.Part.class.isAssignableFrom(rawParameterType)) {
                        throw parameterError(p, "@Part parameters using the MultipartBody.Part must not "
                                + "include a part name in the annotation.");
                    } else {
                        Converter<?, RequestBody> converter =
                                retrofit.requestBodyConverter(type, annotations, methodAnnotations);
                        return new ParameterHandler.Part<>(headers, converter);
                    }
                }

            } else if (annotation instanceof PartMap) {
                if (!isMultipart) {
                    throw parameterError(p, "@PartMap parameters can only be used with multipart encoding.");
                }
                gotPart = true;
                Class<?> rawParameterType = Utils.getRawType(type);
                if (!Map.class.isAssignableFrom(rawParameterType)) {
                    throw parameterError(p, "@PartMap parameter type must be Map.");
                }
                Type mapType = Utils.getSupertype(type, rawParameterType, Map.class);
                if (!(mapType instanceof ParameterizedType)) {
                    throw parameterError(p, "Map must include generic types (e.g., Map<String, String>)");
                }
                ParameterizedType parameterizedType = (ParameterizedType) mapType;

                Type keyType = Utils.getParameterUpperBound(0, parameterizedType);
                if (String.class != keyType) {
                    throw parameterError(p, "@PartMap keys must be of type String: " + keyType);
                }

                Type valueType = Utils.getParameterUpperBound(1, parameterizedType);
                if (MultipartBody.Part.class.isAssignableFrom(Utils.getRawType(valueType))) {
                    throw parameterError(p, "@PartMap values cannot be MultipartBody.Part. "
                            + "Use @Part List<Part> or a different value type instead.");
                }

                Converter<?, RequestBody> valueConverter =
                        retrofit.requestBodyConverter(valueType, annotations, methodAnnotations);

                PartMap partMap = (PartMap) annotation;
                return new ParameterHandler.PartMap<>(valueConverter, partMap.encoding());

            } else if (annotation instanceof Body) {
                if (isFormEncoded || isMultipart) {
                    throw parameterError(p,
                            "@Body parameters cannot be used with form or multi-part encoding.");
                }
                if (gotBody) {
                    throw parameterError(p, "Multiple @Body method annotations found.");
                }

                Converter<?, RequestBody> converter;
                try {
                    converter = retrofit.requestBodyConverter(type, annotations, methodAnnotations);
                } catch (RuntimeException e) {
                    // Wide exception range because factories are user code.
                    throw parameterError(e, p, "Unable to create @Body converter for %s", type);
                }
                gotBody = true;
                return new ParameterHandler.Body<>(converter);
            }

            return null; // Not a Retrofit annotation.
        }

        private void validatePathName(int p, String name) {
            if (!PARAM_NAME_REGEX.matcher(name).matches()) {
                throw parameterError(p, "@Path parameter name must match %s. Found: %s",
                        PARAM_URL_REGEX.pattern(), name);
            }
            // Verify URL replacement name is actually present in the URL path.
            if (!relativeUrlParamNames.contains(name)) {
                throw parameterError(p, "URL \"%s\" does not contain \"{%s}\".", relativeUrl, name);
            }
        }

        /**
         * 获取能将 OkHttp请求数据后返回的ResponseBody 转换成 responseType类型（如：UserInfo）所
         * 对应的对象的一个转换器
         */
        private Converter<ResponseBody, T> createResponseConverter() {
            Annotation[] annotations = method.getAnnotations();
            try {
                return retrofit.responseBodyConverter(responseType, annotations);
            } catch (RuntimeException e) { // Wide exception range because factories are user code.
                throw methodError(e, "Unable to create converter for %s", responseType);
            }
        }

        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            message = String.format(message, args);
            return new IllegalArgumentException(message
                    + "\n    for method "
                    + method.getDeclaringClass().getSimpleName()
                    + "."
                    + method.getName(), cause);
        }

        private RuntimeException parameterError(
                Throwable cause, int p, String message, Object... args) {
            return methodError(cause, message + " (parameter #" + (p + 1) + ")", args);
        }

        private RuntimeException parameterError(int p, String message, Object... args) {
            return methodError(message + " (parameter #" + (p + 1) + ")", args);
        }
    }

    /**
     * Gets the set of unique path parameters used in the given URI. If a parameter is used twice
     * in the URI, it will only show up once in the set.
     */
    static Set<String> parsePathParameters(String path) {
        Matcher m = PARAM_URL_REGEX.matcher(path);
        Set<String> patterns = new LinkedHashSet<>();
        while (m.find()) {
            patterns.add(m.group(1));
        }
        return patterns;
    }

    static Class<?> boxIfPrimitive(Class<?> type) {
        if (boolean.class == type) return Boolean.class;
        if (byte.class == type) return Byte.class;
        if (char.class == type) return Character.class;
        if (double.class == type) return Double.class;
        if (float.class == type) return Float.class;
        if (int.class == type) return Integer.class;
        if (long.class == type) return Long.class;
        if (short.class == type) return Short.class;
        return type;
    }
}
