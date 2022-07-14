import org.gradle.api.Project
import java.util.Properties

fun Project.getLocalPropertyOrEnvVar(property: String, env: String): String? {
    val localPropsFile = rootProject.file("local.properties")
    val properties = Properties().apply {
        load(localPropsFile.inputStream())
    }

    return properties.getProperty(property) ?: System.getenv(env)
}

val Project.ossrhUsername: String?
    get() = getLocalPropertyOrEnvVar(property = "ossrhUsername", env = "OSSRH_USERNAME")

val Project.ossrhPassword: String?
    get() = getLocalPropertyOrEnvVar(property = "ossrhPassword", env = "OSSRH_PASSWORD")

val Project.sonatypeStagingProfileId: String?
    get() = getLocalPropertyOrEnvVar(property = "sonatypeStagingProfileId", env = "SONATYPE_STAGING_PROFILE_ID")

val Project.signingKeyId: String?
    get() = getLocalPropertyOrEnvVar(property = "signingKeyId", env = "SIGNING_KEY_ID")

val Project.signingPassword: String?
    get() = getLocalPropertyOrEnvVar(property = "signingPassword", env = "SIGING_PASSWORD")

val Project.signingKey: String?
    get() = getLocalPropertyOrEnvVar(property = "signingKey", env = "SIGNING_KEY")

val Project.nexusUrl: String
    get() = "https://s01.oss.sonatype.org/service/local/"

val Project.snapshotUrl: String
    get() = "https://s01.oss.sonatype.org/content/repositories/snapshots/"