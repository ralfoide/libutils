/*
 * Project: AndroidAppLib
 * Copyright (C) 2010 ralfoide gmail com,
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

package com.alfray.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

/**
 * Loads keys from keyi/u txt.
 */
public class KeyLoader {

    private final Map<String, String> mKeys = new HashMap<String, String>();

    /**
     * Loads all fields from the keyN.txt file with the specified prefix.
     * Use {@link #getValue(String)} to retrieve fields later.
     *
     * @param context Context to get the resources.
     * @param prefix The prefix to filter from the key file.
     */
    public KeyLoader(Context context, String prefix) {
        String ks[] = null;

        try {
            InputStream is = null;
            try {
                is = context.getResources().getAssets().open("Keyi.txt");
            } catch (Exception e) {
                is = context.getResources().getAssets().open("Keyu.txt");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = null;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(prefix)) continue;

                // split in fields. Ignore the first one which is the prefix.
                String[] fields = line.split(" +");
                for (int n = fields.length - 1; n > 0; n--) {
                    String f = fields[n].trim();
                    int pos = f.indexOf(':');
                    if (pos <= 0 || pos >= f.length()-1) continue;

                    String key = f.substring(0, pos);
                    String value = f.substring(pos + 1);

                    mKeys.put(key, value);
                }
            }
        } catch (Exception e) {
            // ignore silently
        }
    }

    /**
     * Returns the value for a given key, or null if there's no such key.
     */
    public String getValue(String key) {
        return mKeys.get(key);
    }
}
