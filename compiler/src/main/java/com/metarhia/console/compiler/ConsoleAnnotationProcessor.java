package com.metarhia.console.compiler;

import com.google.auto.service.AutoService;
import com.metarhia.console.compiler.annotations.ApiMethod;
import com.metarhia.console.compiler.annotations.MetarhiaObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static javax.annotation.processing.Completions.of;

/**
 * Created by lundibundi on 8/31/16.
 */
@AutoService(Processor.class)
public class ConsoleAnnotationProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    public ConsoleAnnotationProcessor() {
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(MetarhiaObject.class)) {
//            try {

//            } catch (ClassCastException e) {
//                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
//            } catch (IOException e) {
//                messager.printMessage(Diagnostic.Kind.ERROR, "Cannot write class to file: " + e.getMessage());
//            } catch (Exception e) {
//                messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error: " + e.toString());
//            }
        }
        return true;
    }


    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotationMirror,
                                                         ExecutableElement executableElement, String s) {
        if (s.startsWith("A")) {
            return Arrays.asList(of("ApiMethod"));
        } else if (s.startsWith("M")) {
            return Arrays.asList(of("MetarhiaObject"));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Arrays.asList(
                MetarhiaObject.class.getCanonicalName(),
                ApiMethod.class.getCanonicalName()
        ));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}
