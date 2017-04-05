package com.alflabs.dagger;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Qualifies a returned type as belonging to the application (e.g. a Context).
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AppQualifier {
}
