plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "app.nekogram.gifski"
    compileSdk {
        version = release(37)
    }
    ndkVersion = "29.0.14206865"

    defaultConfig {
        minSdk = 23

        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                arguments.addAll(arrayOf("-DANDROID_STL=none", "-DANDROID_PLATFORM=android-23"))
            }
        }
        ndk {
            debugSymbolLevel = "FULL"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "4.1.0+"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    compileOnly(libs.androidx.annotation)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "app.nekogram.gifski"
            artifactId = "gifski"
            version = "1.0.1"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Gifski")
                description.set("Highest-quality GIF encoder")
                url.set("https://github.com/Nekogram/gifski")

                licenses {
                    license {
                        name.set("GNU Affero General Public License version 3.0")
                        url.set("http://www.gnu.org/licenses/agpl-3.0.html")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        name.set("Nekogram")
                        url.set("https://github.com/Nekogram")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/Nekogram/gifski.git")
                    url.set("https://github.com/Nekogram/gifski")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}