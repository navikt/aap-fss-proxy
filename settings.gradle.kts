pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    name = "NavPackageRegistry"
                    url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
                }
            }
            filter {
                includeGroupByRegex("no\\.nav(\\..*)?")
            }
        }
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven {
                    name = "Shibboleth"
                    url = uri("https://build.shibboleth.net/maven/releases/")
                }
            }
            filter {
                includeGroupByRegex("org\\.opensaml(\\..*)?")
                includeGroupByRegex("net\\.shibboleth(\\..*)?")
            }
        }
    }
}

include(":app")

rootProject.name = "aap-fss-proxy"
