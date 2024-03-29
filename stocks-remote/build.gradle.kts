/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  id("com.android.library")
  id("com.google.devtools.ksp")
}

//noinspection GroovyMissingReturnStatement
android {
  namespace = "com.pyamsoft.tickertape.stocks.remote"

  compileSdk = rootProject.extra["compileSdk"] as Int

  defaultConfig {
    minSdk = rootProject.extra["minSdk"] as Int

    resourceConfigurations += setOf("en")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    // Flag to enable support for the new language APIs
    isCoreLibraryDesugaringEnabled = true
  }

  kotlinOptions { jvmTarget = JavaVersion.VERSION_17.getMajorVersion() }

  buildFeatures { buildConfig = false }
}

dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${rootProject.extra["desugar"]}")

  kapt("com.google.dagger:dagger-compiler:${rootProject.extra["dagger"]}")
  ksp("com.squareup.moshi:moshi-kotlin-codegen:${rootProject.extra["moshi"]}")

  implementation("com.squareup.moshi:moshi:${rootProject.extra["moshi"]}")
  implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofit"]}")
  implementation("com.squareup.retrofit2:converter-moshi:${rootProject.extra["retrofit"]}")
  implementation("com.squareup.retrofit2:converter-scalars:${rootProject.extra["retrofit"]}")

  // PYDroid
  implementation("com.github.pyamsoft.pydroid:bootstrap:${rootProject.extra["pydroid"]}")

  // Compose Annotations
  implementation("androidx.compose.runtime:runtime:${rootProject.extra["compose"]}")

  implementation("com.github.pyamsoft:cachify:${rootProject.extra["cachify"]}")

  implementation(project(":core"))
  implementation(project(":stocks"))
  implementation(project(":ui"))
}
