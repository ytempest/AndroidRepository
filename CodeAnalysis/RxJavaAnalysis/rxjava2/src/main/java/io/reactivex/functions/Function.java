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
 * 一个功能接口，可以将一个源类型的对象转换成目的类型的对象，由于在转换过程中有可能会产生异常，
 * 允许将这个异常抛出
 *
 * @param <T> 源类型对象
 * @param <R> 目的类型
 */
public interface Function<T, R> {
    /**
     * 通过一些算法处理源类型对象，并返回一个目的类型的对象
     *
     * @param t 源类型对象
     * @return 目的类型对象
     * @throws Exception on error
     */
    R apply(@NonNull T t) throws Exception;
}
