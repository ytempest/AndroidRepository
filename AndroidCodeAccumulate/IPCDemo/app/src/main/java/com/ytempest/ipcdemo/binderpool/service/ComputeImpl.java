package com.ytempest.ipcdemo.binderpool.service;

import android.os.RemoteException;

import com.ytempest.ipcdemo.binderpool.ICompute;

/**
 * @author ytempest
 *         Description：
 */
public class ComputeImpl extends ICompute.Stub {
    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }
}
