package org.esupportail.sgc.services.crous;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Annotation to indicate that a method requires CROUS API authentication.
 * See @CrousAuthAspect for implementation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireCrousAuth {
}

