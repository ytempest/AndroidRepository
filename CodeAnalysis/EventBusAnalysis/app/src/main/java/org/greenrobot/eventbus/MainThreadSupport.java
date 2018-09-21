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

import android.os.Looper;

/**
 * Interface to the "main" thread, which can be whatever you like. Typically on Android, Android's main thread is used.
 */
public interface MainThreadSupport {

    boolean isMainThread();

    Poster createPoster(EventBus eventBus);

    /**
     * Description：主线程的支持类，通过这个类可以判断当前线程是否为主线程，以及可以获取到
     * 主线程的Handler
     */
    class AndroidHandlerMainThreadSupport implements MainThreadSupport {

        /**
         * 主线程的Looper
         */
        private final Looper looper;

        public AndroidHandlerMainThreadSupport(Looper looper) {
            this.looper = looper;
        }

        /**
         * 判断当前线程是否为主线程
         */
        @Override
        public boolean isMainThread() {
            return looper == Looper.myLooper();
        }

        /**
         * 创建一个主线程的Handler
         */
        @Override
        public Poster createPoster(EventBus eventBus) {
            return new HandlerPoster(eventBus, looper, 10);
        }
    }

}
