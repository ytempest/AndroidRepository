/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.functions;

import io.reactivex.annotations.NonNull;

/**
 * Description：这是一个二对一的功能接口；基于两个不同的源对象进行处理，然后返回一个目的对象
 *
 * @param <T1> 第一个源对象类型
 * @param <T2> 第二个源对象类型
 * @param <R>  目的对象类型
 */
public interface BiFunction<T1, T2, R> {

    /**
     * 基于两个输入对象进行计算，然后返回一个目的类型对象
     *
     * @param t1 第一个输入对象
     * @param t2 第二个输入对象
     * @return 目的类型对象
     * @throws Exception on error
     */
    @NonNull
    R apply(@NonNull T1 t1, @NonNull T2 t2) throws Exception;
}
