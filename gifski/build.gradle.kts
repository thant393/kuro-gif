plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "tw.kurocore.kurogram.gifski"
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
            groupId = "tw.kurocore.kurogram"
            artifactId = "gifski"
            version = "1.0.1"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Gifski")
                description.set("Highest-quality GIF encoder")
                url.set("https://github.com/thant393/kuro-gif")

                licenses {
                    license {
                        name.set("GNU Affero General Public License version 3.0")
                        url.set("http://www.gnu.org/licenses/agpl-3.0.html")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        name.set("Kurogram")
                        url.set("https://github.com/thant393")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/thant393/kuro-gif.git")
                    url.set("https://github.com/thant393/kuro-gif")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}