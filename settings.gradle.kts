/*
 * The settings file is used to specify which projects to include in your build.
 */

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "toy-interpreter"
include("app")
