package com.ytempest.wxpay_independent;

import com.google.auto.service.AutoService;
import com.ytempest.wxpay_annotation.WXPayEntry;

import java.lang.annotation.Annotation;
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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * @author ytempest
 *         Description：使用注解生成器生成微信支付需要定义的Activity回调，这样就不需要在 app模块中
 *         创建关于支付的逻辑，实现支付模块和app模块的逻辑分离
 */
@AutoService(Processor.class)

public class WxpayProssror extends AbstractProcessor {


    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return getSupportAnnotation();
    }

    private Set<String> getSupportAnnotation() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(WXPayEntry.class.getCanonicalName());
        return annotations;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        generateWXPayCode(roundEnv);
        return false;
    }

    private void generateWXPayCode(RoundEnvironment roundEnv) {
        WXPayEntryVisitor visitor = new WXPayEntryVisitor();
        visitor.setFiler(mFiler);
        scanElement(roundEnv, WXPayEntry.class, visitor);
    }

    private void scanElement(RoundEnvironment roundEnv, Class<? extends Annotation> annotation, WXPayEntryVisitor visitor) {
        Set<? extends Element> annotationElements = roundEnv.getElementsAnnotatedWith(annotation);
        // 遍历每一个 注解
        for (Element annotationElement : annotationElements) {
            // 获取元素上所有的注解（只会获取在getSupportedAnnotationTypes()方法中声明的注解）
            List<? extends AnnotationMirror> annotationMirrors = annotationElement.getAnnotationMirrors();
            // 遍历元素上的所有注解
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                // 获取注解的所有属性值，key：属性名称，value：属性值
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();
                visitor.setAnnotationValueCount(elementValues.size());
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                    entry.getValue().accept(visitor, null);
                }
            }
        }
    }

}
