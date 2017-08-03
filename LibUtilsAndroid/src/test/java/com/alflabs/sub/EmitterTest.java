package com.alflabs.sub;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class EmitterTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock ISubscriber<DataSample> mSubscriber;

    private Emitter<DataSample> mEmitter;

    @Before
    public void setUp() throws Exception {
        mEmitter = new Emitter<DataSample>();
    }

    @Test
    public void testSubscribe() throws Exception {
        mEmitter.emit(new DataSample(1, 2));
        verify(mSubscriber, never()).onEmitted(any());

        mEmitter.subscribe(mSubscriber);
        verify(mSubscriber).onEmitted(new DataSample(1, 2));
        reset(mSubscriber);

        mEmitter.emit(new DataSample(3, 4));
        verify(mSubscriber).onEmitted(new DataSample(3, 4));
    }

    private static class DataSample {
        private final int a;
        private final int b;

        public DataSample(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DataSample that = (DataSample) o;

            if (a != that.a) return false;
            return b == that.b;
        }

        @Override
        public int hashCode() {
            int result = a;
            result = 31 * result + b;
            return result;
        }
    }
}
