package com.alflabs.dagger;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a component or class is tied to a fragment's sub-component lifecycle.
 * <p/>
 * See {@link @ActivityScope} for a discussion of what scopes do and how to use them.
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface FragmentScope {
}
