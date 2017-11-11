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

package com.alflabs.rx;

/**
 * Exception thrown by a publisher when trying to attach it to a stream yet it's already attached to another stream.
 */
public class PublisherAttachedException extends RuntimeException {
    static final long serialVersionUID = 1;

    public PublisherAttachedException() {
    }

    public PublisherAttachedException(String message) {
        super(message);
    }

    public PublisherAttachedException(String message, Throwable cause) {
        super(message, cause);
    }
}
