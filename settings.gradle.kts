import java.io.FileInputStream
import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/cyb3rko/jabcode-android")
            credentials {
                val gradleLocalProperties = Properties().apply {
                    load(FileInputStream(File(rootProject.projectDir, "local.properties")))
                }
                username = "cyb3rko"
                password = gradleLocalProperties.getProperty("gpr_token")
            }
        }
    }
}

rootProject.name = "JAB Code Android"
include(":app")
include(":lib")
