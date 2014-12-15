/*
 * Project: Set Sample
 * Copyright (C) 2013 alf.labs gmail com,
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

package android.content;


import android.content.pm.PackageManager;
import android.content.res.Resources;

import java.io.*;

public abstract class Context {
    public static final int MODE_PRIVATE = 0;

    public abstract Context getApplicationContext();

    public abstract File getFileStreamPath(String filename) throws IllegalArgumentException;

    public abstract FileInputStream openFileInput(String filename) throws FileNotFoundException, IllegalArgumentException;

    public abstract FileOutputStream openFileOutput(String filename, int mode) throws FileNotFoundException, IllegalArgumentException;

    public abstract PackageManager getPackageManager();

    public abstract String getPackageName();

    public abstract Resources getResources();
}
