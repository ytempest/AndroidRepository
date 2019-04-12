/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xutils.view;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.xutils.common.util.DoubleKeyValueMap;
import org.xutils.common.util.LogUtil;
import org.xutils.view.annotation.Event;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/*package*/ final class EventListenerManager {

    /**
     * 连续快速点击之间的时间间隔，如果在这个时间间隔内则这个点击不生效
     */
    private final static long QUICK_EVENT_TIME_SPAN = 300;
    private final static HashSet<String> AVOID_QUICK_EVENT_SET = new HashSet<String>(2);

    static {
        AVOID_QUICK_EVENT_SET.add("onClick");
        AVOID_QUICK_EVENT_SET.add("onItemClick");
    }

    private EventListenerManager() {
    }

    /**
     * k1: viewInjectInfo
     * k2: interface Type
     * value: listener
     */
    private final static DoubleKeyValueMap<ViewInfo, Class<?>, Object>
            listenerCache = new DoubleKeyValueMap<ViewInfo, Class<?>, Object>();


    public static void addEventMethod(
            //根据页面或view holder生成的ViewFinder
            ViewFinder finder,
            //根据当前注解ID生成的ViewInfo
            ViewInfo info,
            //注解对象
            Event event,
            //页面或view holder对象
            Object handler,
            //当前注解方法
            Method method) {
        try {
            // 找出要代理的点击的View
            View view = finder.findViewByInfo(info);

            if (view != null) {
                // 注解中定义的接口，比如Event注解默认的接口为View.OnClickListener
                Class<?> listenerType = event.type();
                // 默认为空，注解接口对应的Set方法，比如setOnClickListener方法
                String listenerSetter = event.setter();
                if (TextUtils.isEmpty(listenerSetter)) {
                    listenerSetter = "set" + listenerType.getSimpleName();
                }

                // 获取事件回调要调用的方法
                String methodName = event.method();

                boolean addNewMethod = false;
                /*
                    根据View的ID和当前的接口类型获取已经缓存的接口实例对象，
                    比如根据View.id和View.OnClickListener.class两个键获取这个View的OnClickListener对象
                 */
                Object listener = listenerCache.get(info, listenerType);
                DynamicHandler dynamicHandler = null;
                /*
                    如果接口实例对象不为空
                    获取接口对象对应的动态代理对象
                    如果动态代理对象的handler和当前handler相同
                    则为动态代理对象添加代理方法
                 */
                if (listener != null) {
                    dynamicHandler = (DynamicHandler) Proxy.getInvocationHandler(listener);
                    addNewMethod = handler.equals(dynamicHandler.getHandler());
                    if (addNewMethod) {
                        dynamicHandler.addMethod(methodName, method);
                    }
                }

                // 如果还没有注册此代理
                if (!addNewMethod) {

                    // 代理 Activity 或 View，也就是事件回调所在的类
                    dynamicHandler = new DynamicHandler(handler);

                    // methodName：要回调的方法名称
                    // method：注解标志的方法
                    dynamicHandler.addMethod(methodName, method);

                    // 生成的代理对象实例，比如View.OnClickListener的实例对象
                    listener = Proxy.newProxyInstance(
                            listenerType.getClassLoader(),
                            new Class<?>[]{listenerType},
                            dynamicHandler);

                    listenerCache.put(info, listenerType, listener);
                }

                // 找出 View 回调事件的 set方法
                Method setEventListenerMethod = view.getClass().getMethod(listenerSetter, listenerType);
                // 调用 View 的 set方法设置回调监听，这个回调监听已经通过动态代理代理了，这样的话，这个回调监听就可以
                // 忽略监听器的类型，也就是忽略了是 View.OnClickListener 还是 自定义的 Callback，当回调监听器的方法被执行时，
                // 就会走到动态代理，只需要在动态代理中去手动回调相应的回调方法就可以实现代理了
                // 优点：忽略要进行设置的监听器类型，只要给出设置监听器的方法和监听器的类型就可以实现代理监听器
                setEventListenerMethod.invoke(view, listener);
            }
        } catch (Throwable ex) {
            LogUtil.e(ex.getMessage(), ex);
        }
    }

    public static class DynamicHandler implements InvocationHandler {
        // 存放代理对象，比如Fragment或view holder
        private WeakReference<Object> handlerRef;
        // 存放代理方法，也就是 key 为回调方法名称，value 为回调方法的Method
        private final HashMap<String, Method> methodMap = new HashMap<String, Method>(1);

        private static long lastClickTime = 0;

        public DynamicHandler(Object handler) {
            this.handlerRef = new WeakReference<Object>(handler);
        }

        public void addMethod(String name, Method method) {
            methodMap.put(name, method);
        }

        public Object getHandler() {
            return handlerRef.get();
        }

        /**
         * 当回调监听器的方法被调用时，都会走到这里，在这里可以判断是否调用了我们代理的回调方法，如果有就执行回调方法
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object handler = handlerRef.get();
            if (handler != null) {

                String eventMethod = method.getName();
                if ("toString".equals(eventMethod)) {
                    return DynamicHandler.class.getSimpleName();
                }

                // 如果调用过 addMethod() 方法，那么 methodMap.get(eventMethod) 肯定可以获取到数据
                method = methodMap.get(eventMethod);
                if (method == null && methodMap.size() == 1) {
                    for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
                        if (TextUtils.isEmpty(entry.getKey())) {
                            method = entry.getValue();
                        }
                        break;
                    }
                }

                if (method != null) {

                    // 如果是 onClick() 等方法则
                    if (AVOID_QUICK_EVENT_SET.contains(eventMethod)) {
                        long timeSpan = System.currentTimeMillis() - lastClickTime;
                        // 在 300 毫秒中的所有点击不生效
                        if (timeSpan < QUICK_EVENT_TIME_SPAN) {
                            LogUtil.d("onClick cancelled: " + timeSpan);
                            return null;
                        }
                        lastClickTime = System.currentTimeMillis();
                    }

                    try {
                        return method.invoke(handler, args);
                    } catch (Throwable ex) {
                        throw new RuntimeException("invoke method error:" +
                                handler.getClass().getName() + "#" + method.getName(), ex);
                    }
                } else {
                    LogUtil.w("method not impl: " + eventMethod + "(" + handler.getClass().getSimpleName() + ")");
                }
            }
            return null;
        }
    }
}
