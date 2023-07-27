plugins {
    id("pl.allegro.tech.build.axion-release") version "1.14.4"
}

scmVersion {
    localOnly = true
}
project.version  = scmVersion.version
group = "de.denkunddachte"

allprojects {
    project.version = rootProject.version
}

