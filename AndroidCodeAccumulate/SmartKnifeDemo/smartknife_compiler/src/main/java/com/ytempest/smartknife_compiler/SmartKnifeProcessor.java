package com.ytempest.smartknife_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ytempest.smartknife_annotations.LinkClick;
import com.ytempest.smartknife_annotations.LinkView;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;


/**
 * @author ytempest
 *         Description：
 */
@AutoService(Processor.class)
public class SmartKnifeProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
    }

    /**
     * 指定处理的版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 指定注解处理器是注册给那一个注解的
     */
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

        annotations.add(LinkView.class);
        annotations.add(LinkClick.class);

        return annotations;
    }

    /**
     * 在 getSupportedAnnotationTypes 方法中注册的注解都会走这个方法
     *
     * @param annotations 在 getSupportedAnnotationTypes() 方法中添加的注解都会在这个集合里面
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        // 获取所有的 LinkView 注解
        Set<? extends Element> allLinkViewElement = roundEnvironment.getElementsAnnotatedWith(LinkView.class);
        // 获取所有的 LinkClick 注解
        Set<? extends Element> allLinkClickElement = roundEnvironment.getElementsAnnotatedWith(LinkClick.class);

        Set<Element> allElement = new LinkedHashSet<>();
        allElement.addAll(allLinkViewElement);
        allElement.addAll(allLinkClickElement);

        // 判断是否有元素，如果没有就直接返回，防止多次执行下面的逻辑
        if (!isSetHaveElement(allElement)) {
            return false;
        }

        // 把所有的注解按所属的类块进行分类
        Map<Element, List<Element>> blockElementMap = ClassGenerateHelper
                .classifyElementAccordingBlock(allElement);

        // 用于记录实例化View的id和名称
        HashMap<Integer, String> viewRecordMap = new HashMap<>();

        // 生成每一个类块的的辅助类
        for (Map.Entry<Element, List<Element>> elementListEntry : blockElementMap.entrySet()) {
            Element blockElement = elementListEntry.getKey();
            List<Element> allElementList = elementListEntry.getValue();
            // 获取类块名称
            String blockNameStr = blockElement.getSimpleName().toString();

            // 1、获取构建辅助类结构的 builder
            TypeSpec.Builder classBuilder = ClassGenerateHelper.getClassBuilder(blockElement);

            ///2、获取构建辅助类全局变量的 builder
            FieldSpec.Builder targetFieldBuilder = ClassGenerateHelper.getTargetFieldBuilder(blockElement);
            // 将全局变量添加到构建类的 builder 中
            classBuilder.addField(targetFieldBuilder.build());

            // 3、获取构建辅助类 构造函数的 builder
            MethodSpec.Builder constructorMethodBuilder = ClassGenerateHelper.getConstructorMethodBuilder(blockElement);

            // 4、获取构建辅助类 unbind方法的 builder
            MethodSpec.Builder unbindMethodBuilder = ClassGenerateHelper.getUnbindMethodBuilder(blockElement);


            // 5、遍历类块中的所有注解
            ClassName viewUtilsClassName = ClassName.get(ClassGenerateHelper.SMART_KNIFE_PACKAGE, "ViewUtils");

            for (Element element : allElementList) {

                // 检测Field或Method的修饰符是否正确
                ClassGenerateHelper.checkElementModifier(element);

                if (element.getAnnotation(LinkView.class) != null) {
                    String viewName = element.getSimpleName().toString();
                    int viewId = element.getAnnotation(LinkView.class).value();
                    // 将标记了@LinkView的View添加 target.viewName = ViewUtils.findViewById(target,viewId);
                    constructorMethodBuilder.addStatement("target.$N = $T.findViewById(target, $L)",
                            viewName, viewUtilsClassName, viewId);

                    // 同时，在 unbind 方法中添加 target.viewName = null;
                    unbindMethodBuilder.addStatement("target.$N = null", viewName);

                    viewRecordMap.put(viewId, viewName);


                } else if (element.getAnnotation(LinkClick.class) != null) {
                    String methodName = element.getSimpleName().toString();
                    int viewId = element.getAnnotation(LinkClick.class).value();
                    String viewFieldName = "view" + viewId;

                    // 1、添加View为全局变量
                    FieldSpec.Builder viewFieldBuilder = ClassGenerateHelper.getViewFieldBuilder(viewFieldName);
                    classBuilder.addField(viewFieldBuilder.build());

                    // 2、如果该View已经在 LinkView 中实例化，那就直接引用，否则findViewById
                    if (viewRecordMap.containsKey(viewId)) {
                        constructorMethodBuilder.addStatement(viewFieldName + " = target." + viewRecordMap.get(viewId));
                    } else {
                        constructorMethodBuilder.addStatement(viewFieldName + "= $T.findViewById(target, $L)",
                                viewUtilsClassName, viewId);
                        unbindMethodBuilder.addStatement(viewFieldName + " = null");
                    }

                    // 3、为View设置点击事件
                    ClassName onClickListenerClassName = ClassName.get("android.view", "View.OnClickListener");
                    constructorMethodBuilder.addStatement(
                            viewFieldName + ".setOnClickListener(new $T() {\n" +
                                    "@Override\n" +
                                    "public void onClick(View v) {\n" +
                                    "    target.$N(v);\n" +
                                    "    }\n" +
                                    "})",
                            onClickListenerClassName, methodName);

                    // 4、在 unbind 方法中将点击事件置空
                    unbindMethodBuilder.addStatement(viewFieldName + ".setOnClickListener(null)");
                }
            }
            // 清楚存储的View信息
            viewRecordMap.clear();


            // 6、将方法添加的构建类的 builder 中
            classBuilder.addMethod(constructorMethodBuilder.build());
            classBuilder.addMethod(unbindMethodBuilder.build());

            // 7、开始生成 辅助类
            try {
                String packageName = mElementUtils.getPackageOf(blockElement)
                        .getQualifiedName().toString();
                JavaFile.builder(packageName, classBuilder.build())
                        .addFileComment("The class  generate by SmartKnifeProcessor automatically")
                        .build()
                        .writeTo(mFiler);
            } catch (IOException e) {
                System.out.println("com.ytempest.smartknife_compiler.SmartKnifeProcessor fail to generate class:"
                        + blockNameStr + "_ViewBinding");
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 判断 set 是否有数据
     */
    private boolean isSetHaveElement(Set<? extends Element> elements) {
        if (elements != null) {
            if (elements.size() != 0) {
                return true;
            }
        }
        return false;
    }


}
