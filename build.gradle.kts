plugins {
    id("pl.allegro.tech.build.axion-release") version "1.14.4"
}

scmVersion {
    localOnly()
    tag {
        prefix.set("v")
        branchPrefix.set(mapOf("(?i)(az|allianz|master|HCE).*" to "az"))
    }
    versionCreator { versionFromTag, scmPosition -> versionFromTag + if (scmPosition.branch.matches("(?i)(az|allianz|master|HCE).*".toRegex())) "-az" else "" }

}
project.version  = scmVersion.version
group = "de.denkunddachte"

allprojects {
    project.version = rootProject.version
}

