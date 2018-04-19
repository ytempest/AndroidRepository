package com.ytempest.smartknife_compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author ytempest
 *         Description：构建辅助类的一个帮助类
 */
class ClassGenerateHelper {

    /**
     * 关联的SmartKnife AndroidLibrary的包名
     */
    static final String SMART_KNIFE_PACKAGE = "com.ytempest.smartknife";

    /**
     * 检测 注解标记的属性或方法是否是 private 或 static
     */
    static void checkElementModifier(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        for (Modifier modifier : modifiers) {
            String modifierName = modifier.toString();
            if ("private".equals(modifierName) || "static".equals(modifierName)) {
                throw new IllegalArgumentException("SmartKnife:The modifier of View or method must not be private or static");
            }
        }
    }


    /**
     * 将Element按类块进行分类
     */
    static Map<Element, List<Element>> classifyElementAccordingBlock(Set<? extends Element> elements) {
        Map<Element, List<Element>> blockElementMap = new LinkedHashMap<>();
        for (Element bindViewElement : elements) {
            Element enclosingElement = bindViewElement.getEnclosingElement();
            List<Element> elementList = blockElementMap.get(enclosingElement);
            if (elementList == null) {
                elementList = new ArrayList<>();
                blockElementMap.put(enclosingElement, elementList);
            }
            elementList.add(bindViewElement);
        }
        return blockElementMap;
    }

    /**
     * 获取构建类的 builder
     */
    static TypeSpec.Builder getClassBuilder(Element blockElement) {
        // 组装辅助类:  xxx_ViewBinding implements UnBinder
        // 1、获取类块名
        String blockNameStr = blockElement.getSimpleName().toString();
        ///2、获取 UnBinder 的ClassName，待会创建的辅助类要实现这一个接口
        ClassName unBinderClassName = ClassName.get(SMART_KNIFE_PACKAGE, "UnBinder");
        // 3、开始拼装辅助类的结构
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(blockNameStr + "_ViewBinding")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(unBinderClassName);

        return classBuilder;
    }

    /**
     * 获取构建属性的 builder
     */
    static FieldSpec.Builder getTargetFieldBuilder(Element blockElement) {
        // 获取类块的className
        ClassName blockClassName = ClassName.bestGuess(blockElement.getSimpleName().toString());
        FieldSpec.Builder targetFieldBuilder = FieldSpec
                .builder(blockClassName, "target", Modifier.PRIVATE);

        return targetFieldBuilder;
    }

    /**
     * 获取构建全局View的 builder
     */
    static FieldSpec.Builder getViewFieldBuilder(String viewName) {
        // 获取类块的className
        ClassName blockClassName = ClassName.get("android.view", "View");
        FieldSpec.Builder viewFieldBuilder = FieldSpec
                .builder(blockClassName, viewName, Modifier.PRIVATE);
        return viewFieldBuilder;
    }


    /**
     * 获取构建 unbind 方法的 builder
     */
    static MethodSpec.Builder getUnbindMethodBuilder(Element blockElement) {
        // 1、获取类块的 ClassName
        ClassName blockClassName = ClassName.bestGuess(blockElement.getSimpleName().toString());
        // 2、获取 unbind 方法要标记的 CallSuper注解
        ClassName callSuperClassName = ClassName.get("android.support.annotation", "CallSuper");
        // 3、开始组装 unbind 方法
        MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbind")
                .addStatement("$L target = this.target", blockClassName)
                .addStatement("if (target == null) throw new $T($S)", IllegalStateException.class,
                        "Bindings already cleared.")
                .addStatement("this.target = null")
                .addCode("\n")
                .addAnnotation(Override.class)
                .addAnnotation(callSuperClassName)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);

        return unbindMethodBuilder;
    }

    /**
     * 获取构建构造函数的 builder
     */
    static MethodSpec.Builder getConstructorMethodBuilder(Element blockElement) {
        // 1、获取构造方法要标志的 注解
        ClassName uiThreadClassName = ClassName.get("android.support.annotation", "UiThread");
        // 2、获取构造函数的参数
        ClassName paramsClassName = ClassName.bestGuess(blockElement.getSimpleName().toString());
        // 3、开始拼装构造方法的结构
        MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                .addAnnotation(uiThreadClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(paramsClassName, "target", Modifier.FINAL)
                .addStatement("this.target = target")
                .addCode("\n");

        return constructorMethodBuilder;
    }

}
