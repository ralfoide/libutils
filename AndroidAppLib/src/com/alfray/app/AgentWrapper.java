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

import java.lang.reflect.Method;

import android.content.Context;

/**
 * Wrapper so that we don't depend directly on the agent lib.
 */
public class AgentWrapper {

    //--private static final boolean DEBUG = true;
    public static final String TAG = "LIB-Agent";

    private static Class<?> mAgentClazz;
    private static String mK;

    public enum Event {
        OpenProfileUI,
        OpenTimeActionUI,
        OpenIntroUI,
        OpenErrorReporterUI,
        MenuSettings,
        MenuAbout,
        MenuReset,
        CheckProfiles,
    }


    public AgentWrapper() {
    }

    public void start(Context context) {

        if (mAgentClazz == null) {
            String ks[] = null;

            try {
                KeyLoader kl = new KeyLoader(context, "A");

                ClassLoader cl = context.getClassLoader();
                Class<?> clazz = cl.loadClass(kl.getValue("c"));

                if (clazz != null) {
                    // start ok, keep the class
                    mAgentClazz = clazz;
                    mK = kl.getValue("k");
                }
            } catch (Exception e) {
                // ignore silently
            }
        }

        if (mAgentClazz != null) {
            try {
                Method m = mAgentClazz.getMethod("onStartSession", new Class<?>[] { Context.class, String.class });
                m.invoke(null, new Object[] { context, mK });
            } catch (Exception e) {
                // ignore silently
            }
        }
    }

    public void event(Event event) {
        if (mAgentClazz != null) {
            try {
                Method m = mAgentClazz.getMethod("onEvent", new Class<?>[] { String.class });
                m.invoke(null, new Object[] { event.toString() });
            } catch (Exception e) {
                // ignore silently
            }
        }
    }

    public void stop(Context context) {
        if (mAgentClazz != null) {
            try {
                Method m = mAgentClazz.getMethod("onEndSession", new Class<?>[] { Context.class });
                m.invoke(null, new Object[] { context });
            } catch (Exception e) {
                // ignore silently
            }
        }
    }

}
