package com.alflabs.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class RPairTest {

    @Test
    public void testCreate() throws Exception {
        RPair<String, String> p1 = RPair.create("First", "Second");
        assertThat(p1).isNotNull();
        assertThat(p1.first).isEqualTo("First");
        assertThat(p1.second).isEqualTo("Second");
        assertThat(p1.toString()).isEqualTo("{First, Second}");

        RPair<String, String> p2 = RPair.create("First", "Second");
        assertThat(p1).isEqualTo(p2);
        assertThat(p1).isNotSameAs(p2);

        RPair<String, String> p3 = RPair.create("First", "Third");
        assertThat(p1).isNotEqualTo(p3);
    }
}
