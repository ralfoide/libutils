/*
 * Project: AndroidAppLib
 * Copyright (C) 2012 ralfoide gmail com.
 */

package com.alflabs.app;


/**
 * Holder for app-wide encoded strings.
 */
public class BaseCoreStrings implements ICoreStrings {

    /*
     * $ A="foo" ; python -c "print [ 255^ord(a) for a in '$A' ]" | tr [] {}
     */

    // public static final int EXAMPLE = 0;

    private static char[] Strings0[] = {
       new char[]
            /** Google Analytics UA-12345678-1 -- FOR RELEASE */
            { 0 }
            ,
// DEBUG
//        new char[]
//            /** GA UA-12345678-2 -- FOR PRIVATE BETA */
//            { 0 }
//        ,
            /** Debug key 1 */
            { 0 },
            /** Debug key 2 */
            { 0 },
        };

    public BaseCoreStrings() {
    }

    @Override
    public String get(int code) {
        return decode(Strings0[code]);
    }

    protected String decode(final char[] original) {
        final char[] value = original.clone();

        for (int n = value.length - 1; n >= 0; n--) {
            char c = value[n];
            c = (char) (255 ^ c);
            value[n] = c;
        }
        return new String(value);
    }
}
