/*
 * Project: Lib Utils
 * Copyright (C) 2018 alf.labs gmail com,
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

package com.alflabs.utils;

import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import static com.alflabs.utils.AssertException.assertException;
import static com.google.common.truth.Truth.assertThat;


public class FakeFileOpsTest {
    private FileOps mOps;

    @Before
    public void setUp() throws Exception {
        mOps = new FakeFileOps();
    }

    @Test
    public void testMissingFile() throws Exception {
        File f = new File("some_file");

        assertThat(mOps.isFile(null)).isFalse();
        assertThat(mOps.isFile(f)).isFalse();

        assertException(
                FileNotFoundException.class,
                () -> mOps.toString(f, Charsets.UTF_8));

        assertException(
                FileNotFoundException.class,
                () -> mOps.getProperties(f));
        assertException(
                FileNotFoundException.class,
                () -> mOps.readBytes(f));
    }

    @Test
    public void testWriteFile() throws Exception {
        File f = new File("some_file");
        byte[] actual = "File=Content".getBytes(Charsets.UTF_8);

        mOps.writeBytes(actual, f);

        assertThat(mOps.isFile(f)).isTrue();
        assertThat(mOps.toString(f, Charsets.UTF_8)).isEqualTo("File=Content");
        assertThat(mOps.readBytes(f)).isEqualTo(actual);
        assertThat(mOps.getProperties(f)).containsExactly("File", "Content");
    }

    @Test
    public void testOpenFileWriter() throws IOException {
        File f = new File("some_file");

        mOps.writeBytes("First part ".getBytes(Charsets.UTF_8), f);

        // Write-append
        try (Writer writer1 = mOps.openFileWriter(f, true /* append */)) {
            writer1.write("Second part");
        }

        assertThat(mOps.readBytes(f)).isEqualTo("First part Second part".getBytes(Charsets.UTF_8));

        // Write-replace
        try (Writer writer2 = mOps.openFileWriter(f, false /* append */)) {
            writer2.write("Third part");
        }

        assertThat(mOps.readBytes(f)).isEqualTo("Third part".getBytes(Charsets.UTF_8));
    }
}
