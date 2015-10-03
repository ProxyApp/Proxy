package com.shareyourproxy.api.gson;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an {@link @AutoValue}-annotated type for proper Gson serialization.
 * <p/>
 * This annotation is needed because the {@linkplain Retention retention} of {@code @AutoValue} does
 * not allow reflection at runtime.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoValueClass {

    /**
     * A reference to the AutoValue-generated class (e.g. AutoValue_MyClass). This is necessary to
     * handle obfuscation of the class names.
     */
    Class autoValueClass();
}
