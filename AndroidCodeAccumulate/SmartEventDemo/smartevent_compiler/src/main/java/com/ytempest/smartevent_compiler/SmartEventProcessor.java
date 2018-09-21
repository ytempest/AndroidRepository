package com.ytempest.smartevent_compiler;


import com.google.auto.service.AutoService;
import com.ytempest.smartevent.SmartEventException;
import com.ytempest.smartevent.Subscribe;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    private final String META_PACKAGE_POSITION = "com.ytempest.smartevent";
    private final String INDEX_PACKAGE_CLASS_NAME = "com.ytempest.smartevent.SmartEventIndex";
    /**
     * 存储了所有订阅对象以及其订阅方法的集合
     * key：订阅对象的类全称
     * value：所属的订阅对象的所有订阅方法
     */
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

        // 将订阅方法按其所属的订阅对象进行分类
        classifySubscribers(annotations, roundEnv);

        // 创建索引类
        createInfoIndexFile(INDEX_PACKAGE_CLASS_NAME);

        return false;
    }


    /**
     * 将所有使用 Subscribe注解标注的方法按其所属的订阅对象进行分类，该方法执行完之后，所有的订阅
     * 对象以及其订阅方法都存储在了 mSubscriberWithMethod集合中
     */
    private void classifySubscribers(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 1、遍历 getSupportedAnnotationTypes() 返回的要处理的所有接口
        for (TypeElement annotation : annotations) {

            // 2、如果这个注解是Subscribe
            if (annotation.getQualifiedName().toString().equals(Subscribe.class.getCanonicalName())) {
                // 3、获取用 Subscribe 注解标记的所有方法，然后进行遍历获取订阅方法
                Set<? extends Element> allSubscribeMethodElement = roundEnv.getElementsAnnotatedWith(Subscribe.class);
                for (Element subscribeMethodElement : allSubscribeMethodElement) {

                    // 4、如果这个元素是某个类或某个接口的订阅方法，那么就添加
                    if (subscribeMethodElement instanceof ExecutableElement) {
                        ExecutableElement method = (ExecutableElement) subscribeMethodElement;
                        // 获取该订阅方法所在的订阅对象的全称
                        String subscriberName = method.getEnclosingElement().toString();
                        // 获取这个订阅对象的订阅方法集合
                        Set<ExecutableElement> methodElements = mSubscriberWithMethod.get(subscriberName);
                        if (methodElements == null) {
                            methodElements = new LinkedHashSet<>();
                            mSubscriberWithMethod.put(subscriberName, methodElements);
                        }
                        // 5、检查该订阅方法的修饰符是否符合规范，不符合就抛异常
                        checkModifiers(method);

                        // 6、将该订阅方法加到其所在的订阅对象中
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
        Set<Modifier> modifiers = element.getModifiers();
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
            // 1、获取能写代码到类的Writer对象
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(packageAndClass);
            writer = new BufferedWriter(sourceFile.openWriter());
            String packageName = packageAndClass.substring(0, packageAndClass.lastIndexOf("."));
            String className = packageAndClass.substring(packageAndClass.lastIndexOf(".") + 1);

            // 2、编写类的包信息
            writer.write("package " + packageName + ";\n\n");

            // 3、为索引类导包
            createImportPackageCode(writer);

            // 4、创建索引类的头部
            writer.write("/** This class is generated by SmartEvent, please do not edit. */\n");
            writer.write("public class " + className + " implements SubscriberInfoIndex {\n");

            // 5、创建一些成员属性和静态代码块
            writer.write("\tprivate static final Map<Class<?>, SubscriberInfo> SUBSCRIBER_INDEX;\n\n");
            writer.write("\tstatic {\n");
            writer.write("\t\tSUBSCRIBER_INDEX = new HashMap<Class<?>, SubscriberInfo>();\n\n");

            // 6、为每一个订阅对象创建索引
            writeForEverySubscriber(writer);

            // 7、静态代码块的结尾
            writer.write("\t}\n\n");

            // 8、创建 putIndex() 方法
            createPutIndexMethod(writer);

            // 9、创建 getSubscriberInfo() 方法
            createGetSubscriberInfoMethod(writer);

            // 10、编写索引类的结尾
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
        writer.write("import " + META_PACKAGE_POSITION + ".meta.SubscriberInfoIndex;\n");
        writer.write("import " + META_PACKAGE_POSITION + ".ThreadMode;\n\n");
        writer.write("import java.util.HashMap;\n");
        writer.write("import java.util.Map;\n\n");
    }

    /**
     * 创建 putIndex() 方法
     */
    private void createPutIndexMethod(BufferedWriter writer) throws IOException {
        writer.write("\tprivate static void putIndex(SubscriberInfo info) {\n");
        writer.write("\t\tSUBSCRIBER_INDEX.put(info.getSubscriberClass(), info);\n");
        writer.write("\t}\n\n");
    }

    /**
     * 创建 getSubscriberInfo() 方法
     */
    private void createGetSubscriberInfoMethod(BufferedWriter writer) throws IOException {
        writer.write("\t@Override\n");
        writer.write("\tpublic SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {\n");
        writer.write("\t    SubscriberInfo info = SUBSCRIBER_INDEX.get(subscriberClass);\n");
        writer.write("\t    if (info != null) {\n");
        writer.write("\t        return info;\n");
        writer.write("\t    } else {\n");
        writer.write("\t        return null;\n");
        writer.write("\t    }\n");
        writer.write("\t}\n");
    }


    /**
     * 为每一个订阅对象创建索引
     */
    private void writeForEverySubscriber(BufferedWriter writer) throws IOException {
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
     * .....
     * });
     */
    private void writePutIndex(BufferedWriter writer, String subscriberName,
                               Set<ExecutableElement> subscribeMethodElements) throws IOException {
        // 1、编写PutIndex()方法的开头代码
        writer.write("\t\tputIndex(new SimpleSubscriberInfo("
                + subscriberName + ".class, " +
                "true, " +
                "\n\t\t\tnew SubscriberMethodInfo[] {\n");

        // 2、遍历所有订阅方法，并为每一个订阅方法编写创建SubscriberMethodInfo对象的代码
        writeEverySubscriberMethodInfo(writer, subscribeMethodElements);

        // 3、编写PutIndex()方法的结束代码
        writer.write("\t\t}));\n\n");
    }

    /**
     * 创建订阅对象的所有订阅方法的封装类，如下：
     * new SubscriberMethodInfo("方法名", 参数的全路径, 线程模式，优先级),
     */
    private void writeEverySubscriberMethodInfo(BufferedWriter writer, Set<ExecutableElement> subscribeMethodElements)
            throws IOException {
        for (ExecutableElement methodElement : subscribeMethodElements) {
            Subscribe annotation = methodElement.getAnnotation(Subscribe.class);
            writer.write("\t\t\t\tnew SubscriberMethodInfo("
                    // 方法名
                    + "\"" + methodElement.getSimpleName().toString() + "\"" + ", "
                    // 事件类型的全路径名
                    + getMethodParameterType(methodElement) + ".class" + ", "
                    // 线程模式
                    + "ThreadMode." + annotation.threadMode() + ", "
                    // 优先级
                    + annotation.priority() + "),\n");
        }
    }


    /**
     * 获取方法的事件类型的字符串，如：android.os.Message
     */
    private String getMethodParameterType(ExecutableElement element) {
        if (element != null) {
            // 获取这个方法的字符串，如：onTextChange(android.os.Message)
            String method = element.toString();
            return method.substring(method.indexOf("(") + 1, method.indexOf(")"));
        }
        return null;
    }

}
