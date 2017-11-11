/*
 * Project: Lib Utils
 * Copyright (C) 2012 alf.labs gmail com,
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
