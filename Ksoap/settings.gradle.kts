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
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/ksoap2-android-releases/")
        }
        google()
        mavenCentral()
    }

}

rootProject.name = "RetroRest"
include(":app")
 