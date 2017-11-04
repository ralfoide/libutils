package com.alflabs.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class StringUtilsTest {

    @Test
    public void testCapitalize() throws Exception {
        assertThat(StringUtils.capitalize(null)).isEmpty();
        assertThat(StringUtils.capitalize("")).isEmpty();

        assertThat(StringUtils.capitalize("  ")).isEqualTo("  ");
        assertThat(StringUtils.capitalize("123")).isEqualTo("123");
        assertThat(StringUtils.capitalize("a")).isEqualTo("A");
        assertThat(StringUtils.capitalize("ab")).isEqualTo("Ab");
        assertThat(StringUtils.capitalize("AB")).isEqualTo("Ab");
        assertThat(StringUtils.capitalize("Hello World")).isEqualTo("Hello world");
    }
}
