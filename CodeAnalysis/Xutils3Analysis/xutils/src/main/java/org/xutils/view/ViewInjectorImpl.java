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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xutils.ViewInjector;
import org.xutils.common.util.LogUtil;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;

/**
 * 视图注入的一个实现类，一个单例，会在第一次调用 x.view() 时初始化
 */
public final class ViewInjectorImpl implements ViewInjector {

    private static final HashSet<Class<?>> IGNORED = new HashSet<Class<?>>();

    static {
        IGNORED.add(Object.class);
        IGNORED.add(Activity.class);
        IGNORED.add(android.app.Fragment.class);
        try {
            // 同时忽略 v4包上的
            IGNORED.add(Class.forName("android.support.v4.app.Fragment"));
            IGNORED.add(Class.forName("android.support.v4.app.FragmentActivity"));
        } catch (Throwable ignored) {
        }
    }

    private static final Object lock = new Object();
    private static volatile ViewInjectorImpl instance;

    private ViewInjectorImpl() {
    }

    public static void registerInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ViewInjectorImpl();
                }
            }
        }
        // 初始化完视图注入器后，将注入器交给 Ext管理
        x.Ext.setViewInjector(instance);
    }

    @Override
    public void inject(View view) {
        injectObject(view, view.getClass(), new ViewFinder(view));
    }

    @Override
    public void inject(Activity activity) {
        // 获取Activity的ContentView的注解,并将 ContentView 注入到 Activity
        Class<?> handlerType = activity.getClass();
        try {
            ContentView contentView = findContentView(handlerType);
            if (contentView != null) {
                int viewId = contentView.value();
                if (viewId > 0) {
                    activity.setContentView(viewId);
                }
            }
        } catch (Throwable ex) {
            LogUtil.e(ex.getMessage(), ex);
        }


        // 为 Activity 中的 View 属性进行注入
        injectObject(activity, handlerType, new ViewFinder(activity));
    }

    @Override
    public void inject(Object handler, View view) {
        injectObject(handler, handler.getClass(), new ViewFinder(view));
    }

    @Override
    public View inject(Object fragment, LayoutInflater inflater, ViewGroup container) {
        // inject ContentView
        View view = null;
        Class<?> handlerType = fragment.getClass();
        try {
            ContentView contentView = findContentView(handlerType);
            if (contentView != null) {
                int viewId = contentView.value();
                if (viewId > 0) {
                    view = inflater.inflate(viewId, container, false);
                }
            }
        } catch (Throwable ex) {
            LogUtil.e(ex.getMessage(), ex);
        }

        // inject res & event
        injectObject(fragment, handlerType, new ViewFinder(view));

        return view;
    }

    /**
     * 搜索指定 Class 的 ContentView注解，并支持搜索其父类，但是会忽略某些父类，忽略的类请看
     * {@link ViewInjectorImpl#IGNORED}
     */
    private static ContentView findContentView(Class<?> thisCls) {
        if (thisCls == null || IGNORED.contains(thisCls)) {
            return null;
        }
        ContentView contentView = thisCls.getAnnotation(ContentView.class);
        if (contentView == null) {
            return findContentView(thisCls.getSuperclass());
        }
        return contentView;
    }

    @SuppressWarnings("ConstantConditions")
    private static void injectObject(Object handler, Class<?> handlerType, ViewFinder finder) {

        // 忽略一些不做处理的类
        if (handlerType == null || IGNORED.contains(handlerType)) {
            return;
        }

        // 从父类到子类递归
        injectObject(handler, handlerType.getSuperclass(), finder);

        // 注入视图
        Field[] fields = handlerType.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {

                Class<?> fieldType = field.getType();
                if (
                /* 不注入静态字段 */     Modifier.isStatic(field.getModifiers()) ||
                /* 不注入final字段 */    Modifier.isFinal(field.getModifiers()) ||
                /* 不注入基本类型字段 */  fieldType.isPrimitive() ||
                /* 不注入数组类型字段 */  fieldType.isArray()) {
                    continue;
                }

                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    try {
                        View view = finder.findViewById(viewInject.value(), viewInject.parentId());
                        if (view != null) {
                            field.setAccessible(true);
                            field.set(handler, view);
                        } else {
                            throw new RuntimeException("Invalid @ViewInject for "
                                    + handlerType.getSimpleName() + "." + field.getName());
                        }
                    } catch (Throwable ex) {
                        LogUtil.e(ex.getMessage(), ex);
                    }
                }
            }
        } // end inject view

        // 注入点击事件
        Method[] methods = handlerType.getDeclaredMethods();
        if (methods != null && methods.length > 0) {
            for (Method method : methods) {

                if (Modifier.isStatic(method.getModifiers())
                        || !Modifier.isPrivate(method.getModifiers())) {
                    continue;
                }

                //检查当前方法是否是event注解的方法
                Event event = method.getAnnotation(Event.class);
                if (event != null) {
                    try {
                        // id参数
                        int[] values = event.value();
                        int[] parentIds = event.parentId();
                        int parentIdsLen = parentIds == null ? 0 : parentIds.length;
                        //循环所有id，生成ViewInfo并添加代理反射
                        for (int i = 0; i < values.length; i++) {
                            int value = values[i];
                            if (value > 0) {
                                ViewInfo info = new ViewInfo();
                                info.value = value;
                                info.parentId = parentIdsLen > i ? parentIds[i] : 0;
                                method.setAccessible(true);
                                EventListenerManager.addEventMethod(finder, info, event, handler, method);
                            }
                        }
                    } catch (Throwable ex) {
                        LogUtil.e(ex.getMessage(), ex);
                    }
                }
            }
        } // end inject event

    }

}
