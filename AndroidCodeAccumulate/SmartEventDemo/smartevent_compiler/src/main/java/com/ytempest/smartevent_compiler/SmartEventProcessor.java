package com.ytempest.smartevent_compiler;

import com.google.auto.service.AutoService;
import com.ytempest.smartevent.SmartEventException;
import com.ytempest.smartevent.Subscribe;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * @author ytempest
 *         Description：
 */
@AutoService(Processor.class)
public class SmartEventProcessor extends AbstractProcessor {

    private static final String META_PACKAGE_POSITION = "com.ytempest.smartevent";
    private static final String INDEX_PACKAGE_CLASS_NAME = "com.ytempest.smartevent.SmartEventIndex";
    private final Map<String, Set<ExecutableElement>> mSubscriberWithMethod = new LinkedHashMap<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(Subscribe.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 如果 annotations 长度为0，就说明没有使用 Subscribe注解
        if (annotations.size() == 0) {
            return false;
        }

        // 将订阅方法分类
        classifySubscribers(annotations, roundEnv);

        // 创建索引类
        createInfoIndexFile(INDEX_PACKAGE_CLASS_NAME);

        return false;
    }


    /**
     * 将所有使用 Subscribe注解标注的方法按其所属的订阅对象进行分类
     */
    private void classifySubscribers(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 遍历 getSupportedAnnotationTypes() 返回的要处理的所有接口
        for (TypeElement annotation : annotations) {
            if ("Subscribe".equals(annotation.getSimpleName().toString())) {
                // 获取用 Subscribe 注解标记的所有方法
                Set<? extends Element> subscribeMethodElements = roundEnv.getElementsAnnotatedWith(Subscribe.class);
                for (Element subscribeMethodElement : subscribeMethodElements) {
                    // 如果这个元素是一个方法元素，就添加
                    if (subscribeMethodElement instanceof ExecutableElement) {
                        ExecutableElement method = (ExecutableElement) subscribeMethodElement;
                        String subscriberName = method.getEnclosingElement().toString();
                        Set<ExecutableElement> methodElements = mSubscriberWithMethod.get(subscriberName);
                        if (methodElements == null) {
                            methodElements = new LinkedHashSet<>();
                            mSubscriberWithMethod.put(subscriberName, methodElements);
                        }
                        // 检查修饰符的是否符合规范
                        checkModifiers(method);

                        methodElements.add(method);
                    }
                }
            }
        }
    }

    /**
     * 检查修饰符的是否符合规范
     */
    private void checkModifiers(Element element) {
        List<Modifier> modifiers = new ArrayList<>(element.getModifiers());
        boolean visible = false;
        if (modifiers.contains(Modifier.PUBLIC)) {
            if (modifiers.contains(Modifier.STATIC)
                    || modifiers.contains(Modifier.SYNCHRONIZED)
                    || modifiers.contains(Modifier.ABSTRACT)) {
                visible = false;
            } else {
                visible = true;
            }
        }
        if (!visible) {
            throw new SmartEventException(element.getSimpleName() + " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
        }
    }


    private void createInfoIndexFile(String packageAndClass) {
        BufferedWriter writer = null;
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(packageAndClass);
            writer = new BufferedWriter(sourceFile.openWriter());
            String packageName = packageAndClass.substring(0, packageAndClass.lastIndexOf("."));
            String className = packageAndClass.substring(packageAndClass.lastIndexOf(".") + 1, packageAndClass.length());

            writer.write("package " + packageName + ";\n\n");

            // 为索引类导包
            createImportPackageCode(writer);

            // 创建索引类的主体部分
            writer.write("/** This class is generated by SmartEvent, please do not edit. */\n");
            writer.write("public class " + className + " implements SubscriberInfoIndex {\n");
            writer.write("    private static final Map<Class<?>, SubscriberInfo> SUBSCRIBER_INDEX;\n\n");
            writer.write("    static {\n");
            writer.write("        SUBSCRIBER_INDEX = new HashMap<Class<?>, SubscriberInfo>();\n\n");

            // 为每一个订阅对象创建索引
            writeIndexForSubscriber(writer);

            writer.write("    }\n\n");

            // 创建 putIndex() 方法
            createPutIndexMethod(writer);

            // 创建 getSubscriberInfo() 方法
            createGetSubscriberInfoMethod(writer);

            writer.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException("Could not write source for " + packageAndClass, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    //Silent
                }
            }
        }
    }

    /**
     * 为索引类导包
     */
    private void createImportPackageCode(BufferedWriter writer) throws IOException {
        writer.write("import " + META_PACKAGE_POSITION + ".meta.SimpleSubscriberInfo;\n");
        writer.write("import " + META_PACKAGE_POSITION + ".meta.SubscriberMethodInfo;\n");
        writer.write("import " + META_PACKAGE_POSITION + ".meta.SubscriberInfo;\n");
        writer.write("import " + META_PACKAGE_POSITION + ".meta.SubscriberInfoIndex;\n\n");
        writer.write("import " + META_PACKAGE_POSITION + ".ThreadMode;\n\n");
        writer.write("import java.util.HashMap;\n");
        writer.write("import java.util.Map;\n\n");
    }

    /**
     * 创建 putIndex() 方法
     */
    private void createPutIndexMethod(BufferedWriter writer) throws IOException {
        writer.write("    private static void putIndex(SubscriberInfo info) {\n");
        writer.write("        SUBSCRIBER_INDEX.put(info.getSubscriberClass(), info);\n");
        writer.write("    }\n\n");
    }

    /**
     * 创建 getSubscriberInfo() 方法
     */
    private void createGetSubscriberInfoMethod(BufferedWriter writer) throws IOException {
        writer.write("    @Override\n");
        writer.write("    public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {\n");
        writer.write("        SubscriberInfo info = SUBSCRIBER_INDEX.get(subscriberClass);\n");
        writer.write("        if (info != null) {\n");
        writer.write("            return info;\n");
        writer.write("        } else {\n");
        writer.write("            return null;\n");
        writer.write("        }\n");
        writer.write("    }\n");
    }


    /**
     * 为每一个订阅对象创建索引
     */
    private void writeIndexForSubscriber(BufferedWriter writer) throws IOException {
        for (Map.Entry<String, Set<ExecutableElement>> entry : mSubscriberWithMethod.entrySet()) {
            String subscriberName = entry.getKey();
            Set<ExecutableElement> subscribeMethodElements = entry.getValue();

            writePutIndex(writer, subscriberName, subscribeMethodElements);
        }
    }

    /**
     * 为订阅对象创建这一段代码：
     * putIndex(new SimpleSubscriberInfo(订阅对象的全路径.class, true,
     * new SubscriberMethodInfo[] {
     */
    private void writePutIndex(BufferedWriter writer, String subscriberName,
                               Set<ExecutableElement> subscribeMethodElements) throws IOException {
        writer.write("\t\tputIndex(new SimpleSubscriberInfo(" + subscriberName
                + ".class, true, \n\t\t\tnew SubscriberMethodInfo[] {\n");
        writeSubscriberMethodInfo(writer, subscribeMethodElements);
    }

    /**
     * 创建订阅对象的所有订阅方法的封装类，如下：
     * new SubscriberMethodInfo("方法名", 参数的全路径, 线程模式，优先级),
     */
    private void writeSubscriberMethodInfo(BufferedWriter writer, Set<ExecutableElement> subscribeMethodElements)
            throws IOException {
        for (ExecutableElement methodElement : subscribeMethodElements) {
            Subscribe annotation = methodElement.getAnnotation(Subscribe.class);
            writer.write("\t\t\t\tnew SubscriberMethodInfo("
                    // 方法名
                    + "\"" + methodElement.getSimpleName().toString() + "\"" + ", "
                    // 参数的全路径名
                    + getMethodParameterType(methodElement) + ".class" + ", "
                    // 线程模式
                    + "ThreadMode." + annotation.threadMode() + ", "
                    // 优先级
                    + annotation.priority() + "),\n");

        }

        writer.write("\t\t}));\n\n");
    }


    /**
     * 获取方法的参数类型的字符串
     */
    private String getMethodParameterType(ExecutableElement element) {
        String method = element.toString();
        return method.substring(method.indexOf("(") + 1, method.indexOf(")"));
    }

}
