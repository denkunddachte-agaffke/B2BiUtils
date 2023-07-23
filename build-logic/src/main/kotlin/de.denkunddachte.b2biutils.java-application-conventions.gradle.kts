/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("de.denkunddachte.b2biutils.java-common-conventions")

    // Apply the application plugin to add support for building a CLI application in Java.
    application
    // Apply the java-library plugin for API and implementation separation.
    //`java-library`
    distribution
}

tasks.register("createVersionTxt") {
    doLast {
        var versionFile = file("${rootProject.projectDir}/VERSION.txt").also {
            it.writeText(project.version.toString())
        }
    }
}

tasks.named("jar") {
    finalizedBy("createVersionTxt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}

tasks.distTar {
    compression = Compression.GZIP
    archiveExtension.set("tar.gz")
}
