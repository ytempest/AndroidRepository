package com.ytempest.smartevent;

import com.ytempest.smartevent.meta.SubscriberInfo;
import com.ytempest.smartevent.meta.SubscriberInfoIndex;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ytempest
 *         Description：用于辅助 SmartEvent完成订阅方法的收集功能
 */
class SubscribeMethodFinder {

    /**
     * 订阅方法的修饰符规范
     */
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | Modifier.SYNCHRONIZED;
    /**
     * 缓存了订阅对象的所有订阅方法
     * key：订阅对象
     * value：订阅方法
     */
    private static final ConcurrentHashMap<Class<?>, List<SubscriberMethod>> SUBSCRIBERMETHOD_CACHE = new ConcurrentHashMap<>();

    private boolean isIgnoreGeneratedIndex;
    private List<SubscriberInfoIndex> mSubscriberInfoIndexes = null;

    SubscribeMethodFinder(boolean isIgnoreGeneratedIndex, List<SubscriberInfoIndex> subscriberInfoIndexes) {
        this.isIgnoreGeneratedIndex = isIgnoreGeneratedIndex;
        mSubscriberInfoIndexes = subscriberInfoIndexes;
    }

    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        // 1、获取缓存
        List<SubscriberMethod> subscriberMethods = SUBSCRIBERMETHOD_CACHE.get(subscriberClass);
        if (subscriberMethods != null) {
            return subscriberMethods;
        }

        // 2、没有缓存就重新获取
        if (isIgnoreGeneratedIndex) {
            // 使用放射获取订阅方法
            subscriberMethods = findMethodByReflection(subscriberClass);
        } else {
            // 使用索引获取订阅方法
            subscriberMethods = findMethodByIndex(subscriberClass);
        }

        if (subscriberMethods == null || subscriberMethods.isEmpty()) {
            throw new SmartEventException("Subscriber " + subscriberClass
                    + " and its super classes have no public methods with the @Subscribe annotation");
        } else {
            // 3、缓存当前订阅对象的所有订阅方法，然后返回
            SUBSCRIBERMETHOD_CACHE.put(subscriberClass, subscriberMethods);
            return subscriberMethods;
        }
    }

    /**
     * 通过索引获取所有的订阅方法
     */
    private List<SubscriberMethod> findMethodByIndex(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethodList = new ArrayList<>();
        if (mSubscriberInfoIndexes != null) {
            // 1、遍历所有索引类，找到订阅对象的索引所在的索引类
            for (SubscriberInfoIndex subscriberInfoIndex : mSubscriberInfoIndexes) {
                SubscriberInfo subscriberInfo = subscriberInfoIndex.getSubscriberInfo(subscriberClass);
                // 2、如果是订阅对象所在的索引类
                if (subscriberInfo != null) {
                    // 3、从索引中获取订阅方法
                    SubscriberMethod[] subscriberMethods = subscriberInfo.getSubscriberMethod();
                    // 4、添加所有的订阅方法
                    subscriberMethodList.addAll(Arrays.asList(subscriberMethods));
                }

            }
        } else {
            // 如果没有索引就使用放射
            subscriberMethodList = findMethodByReflection(subscriberClass);
        }
        return subscriberMethodList;
    }

    /**
     * 通过放射获取所有的订阅方法
     */
    private List<SubscriberMethod> findMethodByReflection(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods = new ArrayList<>();

        Method[] methods = subscriberClass.getDeclaredMethods();
        for (Method method : methods) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                // 1、判断方法的修饰符是否正确
                int modifiers = method.getModifiers();
                if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                    // 2、判断参数的数量是否正确
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1) {

                        // 3、解析 Subscribe注解的参数
                        ThreadMode threadMode = subscribe.threadMode();
                        int priority = subscribe.priority();

                        // 4、解析订阅方法的参数
                        Class<?> eventType = parameterTypes[0];

                        // 5、把所有参数封装成一个 Subscription对象
                        SubscriberMethod subscriberMethod = new SubscriberMethod(method, eventType, threadMode, priority);

                        // 6、添加到列表
                        subscriberMethods.add(subscriberMethod);
                    } else {
                        String methodName = method.getName();
                        throw new SmartEventException(methodName +
                                " must have exactly 1 parameter");
                    }
                } else {
                    String methodName = method.getName();
                    throw new SmartEventException(methodName +
                            " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
                }

            }
        }
        return subscriberMethods;
    }

    static void clearCaches() {
        SUBSCRIBERMETHOD_CACHE.clear();
    }
}
