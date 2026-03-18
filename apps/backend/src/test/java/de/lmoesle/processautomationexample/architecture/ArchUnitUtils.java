package de.lmoesle.processautomationexample.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;

public class ArchUnitUtils {

    public static final String IN_PORTS = "..application.ports.in..";
    public static final String OUT_PORTS = "..application.ports.out..";
    public static final String IN_ADAPTERS = "..adapter.in..";
    public static final String OUT_ADAPTERS = "..adapter.out..";
    public static final String APPLICATION = "..application.usecases..";
    public static final String DOMAIN = "..domain..";

    public static final ArchCondition<JavaClass> BE_RECORDS =
            new ArchCondition<>("be records") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    boolean ok = javaClass.isRecord();
                    events.add(new SimpleConditionEvent(
                            javaClass,
                            ok,
                            javaClass.getFullName() + (ok ? " ist ein Record" : " ist KEIN Record")
                    ));
                }
            };

    public static final ArchCondition<JavaClass> BE_RECORDS_OR_INTERFACES =
            new ArchCondition<>("be records or interfaces") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    boolean ok = javaClass.isRecord() || javaClass.isInterface();
                    events.add(new SimpleConditionEvent(
                            javaClass,
                            ok,
                            javaClass.getFullName() + (ok
                                    ? " ist ein Record oder Interface"
                                    : " ist weder Record noch Interface")
                    ));
                }
            };


    public static final ArchCondition<JavaClass> BE_CLASSES =
            new ArchCondition<>("be normal classes (not record/interface/enum/annotation)") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    boolean ok = !javaClass.isRecord()
                            && !javaClass.isInterface()
                            && !javaClass.isEnum()
                            && !javaClass.isAnnotation();
                    events.add(new SimpleConditionEvent(
                            javaClass,
                            ok,
                            javaClass.getFullName() + (ok ? " ist eine Klasse" : " ist keine gültige Klasse")
                    ));
                }
            };

    public static final Class<?>[] SPRING_BEAN_ANNOTATIONS = new Class<?>[]{
            Component.class, Service.class, Repository.class, RestController.class
    };

    public static final ArchCondition<JavaClass> BE_SPRING_BEAN =
            new ArchCondition<>("be a Spring bean") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    boolean ok = false;
                    for (Class<?> ann : SPRING_BEAN_ANNOTATIONS) {
                        if (javaClass.isAnnotatedWith((Class<? extends Annotation>) ann)) {
                            ok = true;
                            break;
                        }
                    }
                    events.add(new SimpleConditionEvent(
                            javaClass,
                            ok,
                            javaClass.getFullName() + (ok ? " is a Spring bean" : " is NOT a Spring bean")
                    ));
                }
            };
}
