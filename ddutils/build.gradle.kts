/*
 * ddutils project
 */

plugins {
    id("de.denkunddachte.b2biutils.java-library-conventions")
}

dependencies {
    /* DataSourcePools helper: */
    implementation("com.mchange:c3p0:0.9.5.5")

    /* Mail API:*/
    api("com.sun.mail:jakarta.mail:2.0.1")
}

