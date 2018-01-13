/*
 * Project: Lib Utils
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


package com.alflabs.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import java.lang.annotation.Target;

/**
 * Marker annotation indicating that this parameter can be null.
 * <p/>
 * The reverse of the @{@link NonNull} annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.LOCAL_VARIABLE })
public @interface Null {
}
