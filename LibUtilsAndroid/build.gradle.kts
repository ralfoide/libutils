/*
 * Project: LibUtils
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

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.alflabs.libutils"
    compileSdk = 37
    enableKotlin = false

    defaultConfig {
        minSdk = 21
//        versionCode = 5
//        versionName = "5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":LibUtilsJava"))

    implementation(libs.androidx.appcompat)
    implementation(libs.guava.android)
    implementation(libs.truth)

    // Dagger generation for main code + for test code
    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
    testImplementation(libs.dagger)
    testAnnotationProcessor(libs.dagger.compiler)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.truth)
    testImplementation(libs.guava.jre)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)

    implementation(libs.rxjava2)
    implementation(libs.rxandroid2)
}
