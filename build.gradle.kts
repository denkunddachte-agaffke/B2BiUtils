plugins {
    id("pl.allegro.tech.build.axion-release") version "1.14.4"
}

scmVersion {
    localOnly = false
    versionCreator("versionWithBranch")
    tag {
        prefix.set("v")
        branchPrefix.set(mapOf("(?i)(az|allianz|master).*" to "az"))
    }

}
project.version  = scmVersion.version
group = "de.denkunddachte"

allprojects {
    project.version = rootProject.version
}

