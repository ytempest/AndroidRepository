package com.ytempest.andfixdemo.andfix;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author ytempest
 *         Description：这个类保存了apatch差分包的相关数据，包括差分包文件、修复了Bug的所有Class文件的全路径名
 */
public class Patch {
    /**
     * 这个文件存储了要修复的bug的Class
     */
    private final String ENTRY_NAME = "META-INF/PATCH.MF";
    /**
     * 保存所有需要修复bug的Class的全类名
     */
    private Map<String, List<String>> mClassMap = new HashMap<>();
    /**
     * apatch差分包，里面保存有修复bug的Class文件，以及相关信息
     */
    private File mPatchFile;


    public Patch(File patchFile) {
        this.mPatchFile = patchFile;
        // 初始化所有修复bug的Class
        init();
    }

    public File getPatchFile() {
        return mPatchFile;
    }

    public Map<String, List<String>> getClassMap() {
        return mClassMap;
    }

    private void init() {
        JarFile jarFile = null;
        InputStream inputStream = null;

        try {
            // 1、打开apatch差分包文件
            // 由于差分包也是一个压缩文件，所以可以直接使用JarFile进行打开
            jarFile = new JarFile(mPatchFile);
            // 获取 META-INF目录下的PATCH.MF文件
            JarEntry jarEntry = jarFile.getJarEntry(ENTRY_NAME);
            inputStream = jarFile.getInputStream(jarEntry);

            // 2、开始解析PATCH.MF文件的流
            // 使用Manifest解析文件流，方便获取PATCH.MF文件中的数据
            Manifest manifest = new Manifest(inputStream);
            // 现在PATCH.MF文件中的数据都以key-value的形式保存在了Attributes中
            Attributes attributes = manifest.getMainAttributes();

            // 3、遍历Attributes，找到我们需要的Class文件并保存
            Attributes.Name attrName;
            for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
                attrName = (Attributes.Name) entry.getKey();
                if (attrName != null) {
                    String name = attrName.toString();
                    // 这些Class文件可能保存在Patch-Classes、Name-Classes等key中
                    if (name.endsWith("Classes")) {
                        // 获取value
                        List<String> list = Arrays.asList(((String) entry.getValue()).split(","));

                        if (name.equalsIgnoreCase("Patch-Classes")) {
                            mClassMap.put(name, list);
                        } else {
                            mClassMap.put(name.trim().substring(0, name.length() - 8), list);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(jarFile);
            close(inputStream);
        }

    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
