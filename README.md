# LibUtils

## What is it

__LibUtils__ is collection of utilities used by all my projects.

## Versions

The git repo is organized using branches:

* __android-lib-v3__ is the latest branch.
    This collection of utility classes is split int a plain Java
    library and an Android library.
    It relies on Gradle and is designed to be integrated into Android
    Studio or IJ Community projects using a simple git submodule import.

* __android-lib-v2__ is very close to v3. It keeps compatibility with
    older versions of Java and Android (mostly before streams).

* __android-lib-v1__ and __android-lib__ are obsolete versions of the
    Android library. The plain Java library had not been extracted yet.

* __master__: The original master branch is a collection of misc C#
    utilities packaged in a pot-pourri library.
    There are two internal directories for .Net 1.1 and .Net 2.0,
    as well as two skeleton apps for C# and Android (I used to clone
    them to get app projects up and running quickly back in the days.)
    This repo is now considered obsolete.

## Content

Here's a non-exhaustive list of goodies available in the current
__android-lib-v3__ branch:

 * [__com.alflabs.rx__](LibUtilsJava/src/main/java/com/alflabs/rx):
   A simplified, lightweight implementation of
   __Reactive Streams__. It is not RxJava compatible. Exists both for
   Java projects and Android projects.

* [__com.alflabs.kv__](LibUtilsJava/src/main/java/com/alflabs/kv):
    A key-value network server/client. The server
    publishes simple key/value pairs and automatically synchronizes
    changed values between the server and all clients.

* [__com.alflabs.serial__](LibUtilsJava/src/main/java/com/alflabs/serial): 
    A simplified object serialization mechanism
    to encode plain Java objects into strings or integer arrays and
    later deserialize them. It is platform independent.
    Comes with an Android specific async file writer/reader.

* __com.alflabs.annotations__: A few helper annotations (null, non
    null, visible for testing, large test) that I was using in my
    projects way before Android had its own annotation support library.

* __com.alflabs.dagger__: A few dagger helper annotation for Android,
    e.g. the typical Activity/Fragment Scope/Qualifier annotations
    that I keep reusing everywhere.

* __com.alflabs.func__: Ad-hoc reimplementation of a minimal set of
    functional interfaces, since Android API < 24 does not have them.

* __com.alflabs.app__ and __com.alflabs.prefs__: Core component classes
    common to many of my Android apps. Generally very ad-hoc and not
    designed to be commonly reused by other projects.

* __com.alflabs.app.v1.Bus__: An event bus implementation, deprecated
    in favor of my RX stream implementation.

* __com.alflabs.utils__: Various utilities (network, logging) common
    to several projects. Also contains Android __ApiHelper__
    compatibility stubs to backport from API 19 back to API 9.

## License

LibUtils is licensed under the __GNU GPL v3 license__.

    Copyright (C) 2008-2017 alf.labs gmail com,

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

The full GPL license is available in the file "LICENSE-gpl-3.0.txt".
