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

import org.greenrobot.eventbus.SubscriberMethod;

/**
 * 封装订阅对象以及该订阅对象所有订阅方法的一些规范
 */
public interface SubscriberInfo {
    /**
     * 获取订阅对象的Class
     */
    Class<?> getSubscriberClass();

    /**
     * 获取订阅对象中的所有订阅方法
     */
    SubscriberMethod[] getSubscriberMethods();

    /**
     * 获取订阅对象父类的所有订阅信息
     */
    SubscriberInfo getSuperSubscriberInfo();

    /**
     * 检查是否允许事件的父类也可以接受事件
     */
    boolean shouldCheckSuperclass();
}
