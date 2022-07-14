plugins {
    alias(libs.plugins.android.lib) apply false
    kotlin("android") version libs.versions.kotlin.get() apply false
    alias(libs.plugins.nexus.publish)
}

nexusPublishing {
    repositories {
        sonatype {
            stagingProfileId.set(project.sonatypeStagingProfileId)
            username.set(project.ossrhUsername)
            password.set(project.ossrhPassword)
            nexusUrl.set(uri(project.nexusUrl))
            snapshotRepositoryUrl.set(uri(project.snapshotUrl))
        }
    }
}