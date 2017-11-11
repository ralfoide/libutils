/*
 * Project: Lib Utils
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
