package com.defecttracker;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class GroupBArchUnitTest {

    private static final Set<String> GROUP_B_CONTROLLERS = Set.of(
            "com.defecttracker.controller.ProjectController",
            "com.defecttracker.controller.DefectController",
            "com.defecttracker.controller.ReleaseController",
            "com.defecttracker.controller.ProjectAllocationController"
    );

    @Test
    @DisplayName("Group (b) Controllers must not expose entities in methods")
    void groupBControllersMustNotExposeEntities() {
        var importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.defecttracker.controller");

        ArchCondition<JavaClass> noEntities = new ArchCondition<>("not expose entity types in methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (!GROUP_B_CONTROLLERS.contains(javaClass.getName())) {
                    return;
                }

                for (JavaMethod method : javaClass.getMethods()) {
                    checkType(method, method.getReturnType(), "return type", events);

                    for (var param : method.getParameters()) {
                        checkType(method, param.getType(), "parameter index " + param.getIndex(), events);
                    }
                }
            }

            private void checkType(JavaMethod method, JavaType type, String location, ConditionEvents events) {
                if (type == null) return;

                JavaClass rawClass = type.toErasure();
                if (rawClass.isAnnotatedWith(Entity.class) || rawClass.getPackageName().startsWith("com.defecttracker.entity")) {
                    String message = String.format("Method %s.%s has entity %s in %s",
                            method.getOwner().getSimpleName(), method.getName(), rawClass.getName(), location);
                    events.add(SimpleConditionEvent.violated(method, message));
                }

                if (type instanceof JavaParameterizedType parameterizedType) {
                    for (JavaType arg : parameterizedType.getActualTypeArguments()) {
                        checkType(method, arg, location, events);
                    }
                }
            }
        };

        classes()
                .that().haveNameMatching(".*Controller")
                .should(noEntities)
                .check(importedClasses);
    }
}
