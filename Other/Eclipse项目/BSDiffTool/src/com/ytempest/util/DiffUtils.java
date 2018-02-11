package com.ytempest.util;

public class DiffUtils {

	static {
		// // 获取当前Java项目的路径
		String projectPath = System.getProperty("user.dir");
		System.load(projectPath + "/lib/BSDiff.dll");
	}

	public static native void diff(String oldApkPath, String newApkPath, String patchPath);
}
