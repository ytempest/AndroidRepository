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

/**
 * Each subscriber method has a thread mode, which determines in which thread the method is to be called by EventBus.
 * EventBus takes care of threading independently from the posting thread.
 *
 * @author Markus
 * @see EventBus#register(Object)
 */
public enum ThreadMode {
    /**
     * （默认）：如果使用事件处理函数指定了线程模型为POSTING，那么该事件在哪个线程发布出来的，
     * 事件处理函数就会在这个线程中运行，也就是说发布事件和接收事件在同一个线程。在线程模型为
     * POSTING的事件处理函数中尽量避免执行耗时操作，因为它会阻塞事件的传递，甚至有可能会引起ANR。
     */
    POSTING,

    /**
     * 事件的处理会在UI线程中执行。事件处理时间不能太长，长了会ANR的。
     */
    MAIN,


    /**
     * 订阅者方法将在主线程（UI线程）中被调用。因此，可以在该模式的订阅者方法中直接更新UI界面。
     * 事件将先进入队列然后才发送给订阅者，所以发布事件的调用将立即返回。这使得事件的处理保持严
     * 格的串行顺序。使用该模式的订阅者方法必须快速返回，以避免阻塞主线程
     */
    MAIN_ORDERED,

    /**
     * 如果事件是在UI线程中发布出来的，那么该事件处理函数就会在新的线程中运行，如果事件本来就是
     * 子线程中发布出来的，那么该事件处理函数直接在发布事件的线程中执行。在此事件处理函数中禁止
     * 进行UI更新操作。
     */
    BACKGROUND,

    /**
     * 如果事件是在UI线程中发布出来的，那么该事件处理函数就会在新的线程中运行，如果事件本来就是
     * 子线程中发布出来的，那么该事件处理函数直接在发布事件的线程中执行。在此事件处理函数中禁止
     * 进行UI更新操作。
     */
    ASYNC
}