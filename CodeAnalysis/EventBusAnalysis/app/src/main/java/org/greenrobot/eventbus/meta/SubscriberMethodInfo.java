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
package org.greenrobot.eventbus.meta;

import org.greenrobot.eventbus.ThreadMode;

/**
 * 使用 apt 编译生成索引类时，会使用这个类封装订阅对象的订阅方法的所有订阅信息
 * 一个 SubscriberMethodInfo 对应一个订阅方法
 */
public class SubscriberMethodInfo {
    /**
     * 订阅方法的名称
     */
    final String methodName;
    final ThreadMode threadMode;
    /**
     * 订阅方法的参数，即接收的事件类型
     */
    final Class<?> eventType;
    final int priority;
    final boolean sticky;

    public SubscriberMethodInfo(String methodName, Class<?> eventType, ThreadMode threadMode,
                                int priority, boolean sticky) {
        this.methodName = methodName;
        this.threadMode = threadMode;
        this.eventType = eventType;
        this.priority = priority;
        this.sticky = sticky;
    }

    /**
     * 使用默认的线程模式，优先级默认为 0，不是粘性事件
     */
    public SubscriberMethodInfo(String methodName, Class<?> eventType) {
        this(methodName, eventType, ThreadMode.POSTING, 0, false);
    }

    /**
     * 优先级默认为 0，不是粘性事件
     */
    public SubscriberMethodInfo(String methodName, Class<?> eventType, ThreadMode threadMode) {
        this(methodName, eventType, threadMode, 0, false);
    }

}