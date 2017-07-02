package com.alflabs.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class InjectionValidatorTest {
    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSuccess() throws Exception {
        InjectionValidator.check(new AllInjectedSub());
    }

    @Test
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void testFailure() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Not true that \"field2\" is a non-null reference");
        InjectionValidator.check(new NotAllInjectedSub());
    }

    public static class AllInjectedBase {
        @Inject Object field1;
        @Inject Double field2;

        public AllInjectedBase() {
            this.field1 = "Foo";
            this.field2 = 42.0;
        }
    }

    public static class AllInjectedSub extends AllInjectedBase {
        @Inject String field3;

        public AllInjectedSub() {
            this.field3 = "Bar";
        }
    }

    public static class NotAllInjectedBase {
        @Inject Object field1;
        @Inject Double field2;

        public NotAllInjectedBase() {
            this.field1 = "Foo";
            // this.field2 is not set
        }
    }

    public static class NotAllInjectedSub extends NotAllInjectedBase {
        @Inject String field3;

        public NotAllInjectedSub() {
            this.field3 = "Bar";
        }
    }

}
