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

package com.alflabs.utils;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Utility class that executes methods from Java's {@link File} or Guava's {@link Files}
 * in a way that is easy to mock.
 * <p/>
 * This does not have an automatic dagger inject constructor. Instead it would typically
 * provided explicitly in a module, which allows unit tests to override it easily:
 * <pre>
 *     \@Singleton
 *     \@Provides
 *     public FileOps provideFileOps() {
 *         return new FileOps();
 *     }
 * </pre>
 */
public class FileOps {

    /**
     * Returns true if the file path points to a real file (and not a directory).
     *
     * @param file A {@link File} path, possibly null.
     * @return True if it points to a file; false if null.
     */
    public boolean isFile(@Null File file) {
        return file != null && file.isFile();
    }

    /**
     * Returns true if the directory path points to a real directory (and not a file).
     *
     * @param directory A {@link File} path, possibly null.
     * @return True if it points to a directory; false if null.
     */
    public boolean isDir(@Null File directory) {
        return directory != null && directory.isDirectory();
    }

    /**
     * Reads all characters from a file into a {@link String}, using the given character set.
     *
     * @param file The file to read from.
     * @param charset The charset used to decode the input stream; see {@link StandardCharsets} for
     *     helpful predefined constants.
     * @return a string containing all the characters from the file.
     * @throws IOException if an I/O error occurs.
     */
    @NonNull
    public String toString(@NonNull File file, @NonNull Charset charset) throws IOException {
        return Files.toString(file, charset);
    }

    /**
     * Reads a Java properties file.
     *
     * @param file The file to read from
     * @return A non-null {@link Properties} object.
     * @throws FileNotFoundException if the file does not exist.
     * @throws IOException if the file cannot be parsed into properties.
     */
    @NonNull
    public Properties getProperties(@NonNull File file) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        return props;
    }

    /**
     * Creates any necessary but nonexistent parent directories of the specified file.
     *
     * @throws IOException if an I/O error occurs
     * @see Files#createParentDirs(File)
     */
    public void createParentDirs(@NonNull File file) throws IOException {
        Files.createParentDirs(file);
    }

    /**
     * Overwrites a file with the contents of a byte array.
     *
     * @throws IOException if an I/O error occurs
     * @see Files#write(byte[], File)
     */
    public void writeBytes(byte[] bytes, File file) throws IOException {
        Files.write(bytes, file);
    }

    /**
     * Reads all bytes from file as a byte array.
     *
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the file is bigger than MAX_INT (2^31-1)
     * @see Files#toByteArray(File)
     */
    public byte[] readBytes(File file) throws IOException {
        return Files.toByteArray(file);
    }

    /**
     * Returns a new {@link FileWriter} that can create or append characters to the given file.
     * <p/>
     * Tip: Use this in a Java-7 style resource block, e.g. {@code try(openFileWriter(...))} to
     * get the file closed automatically.
     * <p/>
     * This returns a {@link Writer} so that mocks/fakes can use {@link StringWriter} instead of
     * an actual file.
     *
     * @throws IOException if an I/O error occurs
     * @see FileWriter
     */
    public Writer openFileWriter(File file, boolean append) throws IOException {
        return new FileWriter(file, append);
    }

    /** Utility method that converts a list of "folder1/...folderN/leafName" to a File. */
    @NonNull
    public File toFile(@NonNull String...names) {
        File f = null;
        for (String name : names) {
            f = f == null ? new File(name) : new File(f, name);
        }
        return f;
    }
}
