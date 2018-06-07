//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.reactivestreams;

public interface Subscription {
    void request(long var1);

    void cancel();
}
