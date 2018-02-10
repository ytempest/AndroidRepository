package com.ytempest.util;

public class DiffUtils {

	static {
		String previousPath = System.getProperty("user.dir");
		System.load(previousPath + "/lib/BSDiff.dll");
	}

	public static native void diff(String oldApkPath, String newApkPath, String patchPath);
}
