package com.alflabs.dagger;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a component or class is tied to an activity's sub-component lifecycle.
 * <p/>
 * A scope is used to tag a component. Every class tagged with the same scope becomes
 * a "singleton" tied to the lifecycle of that component. When a given sub-component is
 * tagged with a scope, all the modules provided classes should logically have the same
 * scope (if they are singletons within this scope) or no scope (if they are not singletons).
 * <p/>
 * For example, given this setup:
 * <pre>
 *     @ActivityScope @Subcomponent public interface ISomeActivityComponent { ... }
 *     @ActivityScope class SomePresenter { ... }
 *     class SomeActivity { @Inject SomePresenter ... }
 * </pre>
 * The presenter injected in the activity will be singleton tied to the "activity scope".
 * In Dagger it is implemented as a DoubleCheck member of the Component implementation and returned to
 * all users injected by the component.
 * <p/>
 * There is no need for an "@ApplicationScope", since that's what the basic @Singleton does.
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityScope {
}
