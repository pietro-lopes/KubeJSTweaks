package dev.uncandango.kubejstweaks.mixin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConditionalMixin {
    String modId();

    String versionRange();

    String[] extraModDep() default {};

    String[] extraModDepVersions() default {};

    String config() default "";

    boolean devOnly() default false;
}
