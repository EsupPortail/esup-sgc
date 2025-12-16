package org.esupportail.sgc.services.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    * Annotation to indicate that a method requires REST authentication.
    * See @RestAuthAspect for implementation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireRestAuth {
}
