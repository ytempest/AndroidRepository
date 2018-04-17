/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
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
package org.greenrobot.eventbus;

import android.util.Log;

import org.greenrobot.eventbus.meta.SubscriberInfo;
import org.greenrobot.eventbus.meta.SubscriberInfoIndex;
import org.greenrobot.eventbus.meta.SubscriberMethodInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SubscriberMethodFinder {
    /*
     * In newer class files, compilers may add methods. Those are called bridge or synthetic methods.
     * EventBus must ignore both. There modifiers are not public but defined in the Java class file format:
     * http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6-200-A.1
     */
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;
    private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE = new ConcurrentHashMap<>();

    private List<SubscriberInfoIndex> subscriberInfoIndexes;
    private final boolean strictMethodVerification;
    /**
     * 是否忽略索引，默认不忽略，如果没有索引就会使用放射
     */
    private final boolean ignoreGeneratedIndex;

    private static final int POOL_SIZE = 4;
    private static final FindState[] FIND_STATE_POOL = new FindState[POOL_SIZE];

    SubscriberMethodFinder(List<SubscriberInfoIndex> subscriberInfoIndexes, boolean strictMethodVerification,
                           boolean ignoreGeneratedIndex) {
        Log.e(TAG.string(), "SubscriberMethodFinder: subscriberInfoIndexes --> " + subscriberInfoIndexes);
        this.subscriberInfoIndexes = subscriberInfoIndexes;
        this.strictMethodVerification = strictMethodVerification;
        this.ignoreGeneratedIndex = ignoreGeneratedIndex;
    }

    /**
     * 获取订阅对象的所有订阅方法，通过索引或者放射
     *
     * @param subscriberClass 订阅对象
     * @return 存储了目标对象所有标记了 Subscribe注解方法的信息的一个列表
     */
    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        // 先从缓存中查找有没有这个对象，如果用 MainActivity启动了MainActivity，
        // 就会把上一个MainActivity遍历好的SubscriberMethod列表返回，而不会重新遍历封装
        List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
        if (subscriberMethods != null) {
            return subscriberMethods;
        }

        // 是否忽略注解器生成MyEventBusIndex，ignoreGeneratedIndex就为true，默认为false
        if (ignoreGeneratedIndex) {
            // 利用反射获取订阅对象（如MainActivity）的所有订阅方法的信息（订阅方法的参数、Subscribe注解的参数）
            subscriberMethods = findUsingReflection(subscriberClass);
        } else {
            // 如果使用了注解生成器，就会像ButterKnife那样会生成索引类
            subscriberMethods = findUsingInfo(subscriberClass);
        }
        // 如果订阅对象以及其父类没有定义订阅方法，那么就会抛异常
        if (subscriberMethods.isEmpty()) {
            throw new EventBusException("Subscriber " + subscriberClass
                    + " and its super classes have no public methods with the @Subscribe annotation");
        } else {
            // 缓存
            METHOD_CACHE.put(subscriberClass, subscriberMethods);
            return subscriberMethods;
        }
    }


    /**
     * 通过索引获取到指定订阅对象的所有的订阅方法，如果没有索引就使用放射
     *
     * @param subscriberClass 订阅对象的Class
     */
    private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
        // 获取 FindState对象
        FindState findState = prepareFindState();
        // 将 FindState对象封装 订阅对象
        findState.initForSubscriber(subscriberClass);
        while (findState.clazz != null) {
            // 获取当前订阅对象的所有订阅方法
            findState.subscriberInfo = getSubscriberInfo(findState);
            // 如果使用了索引
            if (findState.subscriberInfo != null) {
                /**
                 * {@link org.greenrobot.eventbus.meta.SimpleSubscriberInfo}
                 * 获取到订阅对象的所有订阅方法
                 */
                SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
                for (SubscriberMethod subscriberMethod : array) {
                    if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                        findState.subscriberMethods.add(subscriberMethod);
                    }
                }
            } else {
                // 如果没有使用索引就使用放射去获取
                Log.e(TAG.string(), "findUsingInfo: 通过放射获取订阅方法");
                findUsingReflectionInSingleClass(findState);
            }
            findState.moveToSuperclass();
        }
        return getMethodsAndRelease(findState);
    }

    private List<SubscriberMethod> getMethodsAndRelease(FindState findState) {
        List<SubscriberMethod> subscriberMethods = new ArrayList<>(findState.subscriberMethods);
        findState.recycle();
        synchronized (FIND_STATE_POOL) {
            for (int i = 0; i < POOL_SIZE; i++) {
                if (FIND_STATE_POOL[i] == null) {
                    FIND_STATE_POOL[i] = findState;
                    break;
                }
            }
        }
        return subscriberMethods;
    }

    private FindState prepareFindState() {
        synchronized (FIND_STATE_POOL) {
            for (int i = 0; i < POOL_SIZE; i++) {
                FindState state = FIND_STATE_POOL[i];
                if (state != null) {
                    FIND_STATE_POOL[i] = null;
                    return state;
                }
            }
        }
        return new FindState();
    }

    /**
     * {@link org.greenrobot.eventbus.meta.AbstractSubscriberInfo}
     */
    private SubscriberInfo getSubscriberInfo(FindState findState) {
        if (findState.subscriberInfo != null
                && findState.subscriberInfo.getSuperSubscriberInfo() != null) {
            SubscriberInfo superclassInfo = findState.subscriberInfo.getSuperSubscriberInfo();
            if (findState.clazz == superclassInfo.getSubscriberClass()) {
                return superclassInfo;
            }
        }

        // 如果是第一次调用这个方法就会走这里的逻辑
        if (subscriberInfoIndexes != null) {
            for (SubscriberInfoIndex index : subscriberInfoIndexes) {
                // 根据订阅对象的Class获取订阅对象中的所有订阅方法（这些方法都封装在 SubscriberInfo中）
                SubscriberInfo info = index.getSubscriberInfo(findState.clazz);
                if (info != null) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 通过反射把目标对象所有有标记了 Subscribe注解的方法解析，然后封装成 SubscriberMethod对象，
     * 接着把这些对象添加到一个List列表中，最后把这个列表返回
     *
     * @param subscriberClass 订阅对象的Class
     */
    private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass) {
        FindState findState = prepareFindState();
        findState.initForSubscriber(subscriberClass);
        while (findState.clazz != null) {
            findUsingReflectionInSingleClass(findState);
            findState.moveToSuperclass();
        }
        return getMethodsAndRelease(findState);
    }

    private void findUsingReflectionInSingleClass(FindState findState) {
        Method[] methods;
        try {
            // 通过放射获取订阅对象的所有方法
            methods = findState.clazz.getDeclaredMethods();
        } catch (Throwable th) {
            // 这里好像是要修复某些bug
            // 详细请看：https://github.com/greenrobot/EventBus/issues/149
            methods = findState.clazz.getMethods();
            findState.skipSuperClasses = true;
        }
        // 遍历订阅对象的所有方法
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                // 获取订阅方法的参数集合
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    // 获取订阅方法标记的Subscribe注解
                    Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                    if (subscribeAnnotation != null) {
                        // 获取订阅方法的第一个参数
                        Class<?> eventType = parameterTypes[0];
                        if (findState.checkAdd(method, eventType)) {
                            // 获取订阅方法的线程模式
                            ThreadMode threadMode = subscribeAnnotation.threadMode();
                            // 将订阅方法的所有信息封装在 SubscriberMethod对象中，并添加到列表中
                            findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,
                                    subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
                        }
                    }
                } else {
                    if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                        // 如果订阅方法的参数不是只有一个也会抛异常
                        String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                        throw new EventBusException("@Subscribe method " + methodName +
                                "must have exactly 1 parameter but has " + parameterTypes.length);
                    }
                }
            } else {
                if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                    // 如果订阅方法是 static、abstract或者不是 public，就会抛异常
                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                    throw new EventBusException(methodName +
                            " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
                }
            }
        }
    }

    static void clearCaches() {
        METHOD_CACHE.clear();
    }

    /**
     * 这个类封装了根据订阅对象查找其所有订阅方法的所有状态和信息
     */
    static class FindState {
        final List<SubscriberMethod> subscriberMethods = new ArrayList<>();
        final Map<Class, Object> anyMethodByEventType = new HashMap<>();
        final Map<String, Class> subscriberClassByMethodKey = new HashMap<>();
        final StringBuilder methodKeyBuilder = new StringBuilder(128);

        Class<?> subscriberClass;
        Class<?> clazz;
        /**
         * 是否跳过父类
         */
        boolean skipSuperClasses;
        SubscriberInfo subscriberInfo;

        void initForSubscriber(Class<?> subscriberClass) {
            this.subscriberClass = clazz = subscriberClass;
            skipSuperClasses = false;
            subscriberInfo = null;
        }

        void recycle() {
            subscriberMethods.clear();
            anyMethodByEventType.clear();
            subscriberClassByMethodKey.clear();
            methodKeyBuilder.setLength(0);
            subscriberClass = null;
            clazz = null;
            skipSuperClasses = false;
            subscriberInfo = null;
        }

        boolean checkAdd(Method method, Class<?> eventType) {
            // 2 level check: 1st level with event type only (fast), 2nd level with complete signature when required.
            // Usually a subscriber doesn't have methods listening to the same event type.
            Object existing = anyMethodByEventType.put(eventType, method);
            if (existing == null) {
                return true;
            } else {
                if (existing instanceof Method) {
                    if (!checkAddWithMethodSignature((Method) existing, eventType)) {
                        // Paranoia check
                        throw new IllegalStateException();
                    }
                    // Put any non-Method object to "consume" the existing Method
                    anyMethodByEventType.put(eventType, this);
                }
                return checkAddWithMethodSignature(method, eventType);
            }
        }

        private boolean checkAddWithMethodSignature(Method method, Class<?> eventType) {
            methodKeyBuilder.setLength(0);
            methodKeyBuilder.append(method.getName());
            methodKeyBuilder.append('>').append(eventType.getName());

            String methodKey = methodKeyBuilder.toString();
            Class<?> methodClass = method.getDeclaringClass();
            Class<?> methodClassOld = subscriberClassByMethodKey.put(methodKey, methodClass);
            if (methodClassOld == null || methodClassOld.isAssignableFrom(methodClass)) {
                // Only add if not already found in a sub class
                return true;
            } else {
                // Revert the put, old class is further down the class hierarchy
                subscriberClassByMethodKey.put(methodKey, methodClassOld);
                return false;
            }
        }

        void moveToSuperclass() {
            if (skipSuperClasses) {
                clazz = null;
            } else {
                clazz = clazz.getSuperclass();
                String clazzName = clazz.getName();
                /** Skip system classes, this just degrades performance. */
                if (clazzName.startsWith("java.") || clazzName.startsWith("javax.") || clazzName.startsWith("android.")) {
                    clazz = null;
                }
            }
        }
    }

}
