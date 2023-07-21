/*
 * ddutils project
 */

plugins {
    id("de.denkunddachte.b2biutils.java-library-conventions")
}

dependencies {
	// api dep from gradle init (remove when sources are removed) 
    api(project(":B2BApiClient"))

	// DataSourcePools helper:
    implementation("com.mchange:c3p0:0.9.5.5")
}

