package com.defecttracker;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.dto.response.PaginatedResponse;
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

import java.util.List;
import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class GroupAArchUnitTest {

    private static final Set<String> GROUP_A_CONTROLLERS = Set.of(
            "com.defecttracker.controller.EmployeeController",
            "com.defecttracker.controller.RoleController",
            "com.defecttracker.controller.PermissionController",
            "com.defecttracker.controller.EmailConfigurationController",
            "com.defecttracker.controller.KlocController"
    );

    @Test
    @DisplayName("Group (a) Controllers must not expose entities or Object/wildcard generic arguments")
    void groupAControllersMustNotExposeEntitiesOrObjectGenerics() {
        var importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.defecttracker.controller");

        ArchCondition<JavaClass> noEntitiesOrObjectGenerics = new ArchCondition<>("not expose entity types or Object/wildcard generics in methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (!GROUP_A_CONTROLLERS.contains(javaClass.getName())) {
                    return;
                }

                for (JavaMethod method : javaClass.getMethods()) {
                    // Check return type
                    checkType(method, method.getReturnType(), "return type", events);

                    // Check parameters
                    for (var param : method.getParameters()) {
                        checkType(method, param.getType(), "parameter index " + param.getIndex(), events);
                    }
                }
            }

            private void checkType(JavaMethod method, JavaType type, String location, ConditionEvents events) {
                if (type == null) return;

                JavaClass rawClass = type.toErasure();
                // Check entity leak
                if (rawClass.isAnnotatedWith(Entity.class) || rawClass.getPackageName().startsWith("com.defecttracker.entity")) {
                    String message = String.format("Method %s.%s has entity %s in %s",
                            method.getOwner().getSimpleName(), method.getName(), rawClass.getName(), location);
                    events.add(SimpleConditionEvent.violated(method, message));
                }

                // If parameterized type, check for Object / wildcard generic arguments or entities
                if (type instanceof JavaParameterizedType parameterizedType) {
                    List<JavaType> typeArguments = parameterizedType.getActualTypeArguments();
                    boolean isApiResponseOrPaginated = rawClass.isAssignableTo(ApiResponse.class)
                            || rawClass.isAssignableTo(PaginatedResponse.class)
                            || rawClass.getSimpleName().equals("ResponseEntity");

                    for (JavaType arg : typeArguments) {
                        String argName = arg.getName();
                        if (isApiResponseOrPaginated && ("java.lang.Object".equals(argName) || argName.startsWith("?"))) {
                            String message = String.format("Method %s.%s exposes Object or wildcard generic argument %s in %s",
                                    method.getOwner().getSimpleName(), method.getName(), argName, location);
                            events.add(SimpleConditionEvent.violated(method, message));
                        }
                        checkType(method, arg, location, events);
                    }
                }
            }
        };

        classes()
                .that().haveNameMatching(".*Controller")
                .should(noEntitiesOrObjectGenerics)
                .check(importedClasses);
    }
}
