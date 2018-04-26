package com.ytempest.rxjavaanalysis.rxjava;

import com.ytempest.rxjavaanalysis.rxjava.Observer;

/**
 * @author ytempest
 *         Description：
 */
public interface ObservableSource<T> {
    void subscribe(Observer<T> observer);
}
