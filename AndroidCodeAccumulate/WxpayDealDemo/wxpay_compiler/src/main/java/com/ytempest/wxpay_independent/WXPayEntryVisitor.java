package com.ytempest.wxpay_independent;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor7;

/**
 * @author ytempest
 *         Description：这个类是用于获取 AnnotationValue对象中封装的注解的属性的值
 */
public class WXPayEntryVisitor extends SimpleAnnotationValueVisitor7<Void, Void> {

    private final static String CLASS_NAME = "WXPayEntryActivity";
    private String mPackageName;
    private Filer mFiler;
    private TypeMirror mTypeMirror;
    private int mAnnotationValueCount;
    private int mVisitedCount = 0;

    /**
     * 这个方法会获取 AnnotationValue（这个是注解的值的封装对象，里面有指定类型的值）中的String值
     *
     * @param s 注解的String类型的值
     */
    @Override
    public Void visitString(String s, Void aVoid) {
        mPackageName = s;
        generateWXPayCode();
        return aVoid;
    }

    /**
     * 这个方法会获取 AnnotationValue（这个是注解的值的封装对象，里面有指定类型的值）中的对象值
     *
     * @param t 注解的对象类型的值（如Class<?>）
     */
    @Override
    public Void visitType(TypeMirror t, Void aVoid) {
        mTypeMirror = t;
        generateWXPayCode();
        return aVoid;
    }

    private void generateWXPayCode() {
        mVisitedCount++;
        if (mVisitedCount != mAnnotationValueCount) {
            return;
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(TypeName.get(mTypeMirror));

        try {
            JavaFile.builder(mPackageName + ".wxapi", classBuilder.build())
                    .addFileComment("This class auto generate by WXPayEntry")
                    .build()
                    .writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("auto generate class " + CLASS_NAME + " fail, Please check");
        }
    }

    public void setFiler(Filer filer) {
        mFiler = filer;
    }

    public void setAnnotationValueCount(int annotationValueCount) {
        mVisitedCount = 0;
        mAnnotationValueCount = annotationValueCount;
    }
}
