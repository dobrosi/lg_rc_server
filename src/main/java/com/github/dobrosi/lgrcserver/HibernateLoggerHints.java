package com.github.dobrosi.lgrcserver;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;

public class HibernateLoggerHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        try {
            Class<?> loggerClass = Class.forName("org.hibernate.jpa.internal.JpaLogger");
            hints.reflection().registerType(loggerClass,
                                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                                            MemberCategory.INVOKE_PUBLIC_METHODS,
                                            MemberCategory.DECLARED_FIELDS
            );
        } catch (ClassNotFoundException e) {
            // Kezelés, ha nem található
        }
    }
}
