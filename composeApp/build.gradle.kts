import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

room {
    schemaDirectory("$projectDir/src/commonMain/schemas")
}

dependencies {
    ksp(libs.roomCompiler)
}


kotlin {
    jvm("desktop")

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)

                // Database
                implementation(libs.roomRuntime)
                implementation(libs.roomGuava)
                implementation(libs.roomKtx)
                implementation(libs.roomBundled)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.navigation.compose)

                // JSON Processing
                api(libs.kotlinx.serialization.json)

                // Tools
                api(libs.guava)
                implementation(libs.hutool.all)

                // Dependency Injection
                api(libs.koin.core)
                api(libs.koin.test)

                // Spider Dependencies
                api(libs.json.org)
                implementation(libs.gson)
                implementation(libs.sardine)
                implementation(libs.jsoupxpath)
                implementation(libs.jsoup)
                implementation(libs.zxing.core)
                implementation(libs.nanohttpd)
                implementation(libs.zstd.jni)

                // Ktor HTTP Server & Client
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.cors)
                implementation(libs.ktor.server.default.headers)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.server.swagger)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.server.html.builder)
                implementation(libs.ktor.server.websockets)

                // Kotlinx HTML
                implementation(libs.kotlinx.html)

                // Logging
                implementation(libs.log4j.core)
                implementation(libs.log4j.api)
                implementation(libs.log4j.slf4j2.impl)  // SLF4J 到 Log4j2 的桥接
                implementation(libs.jansi)

                // Image Loader
                api(libs.image.loader)

                // OkHttp
                api(project.dependencies.platform(libs.okhttp.bom))
                api(libs.okhttp)
                api(libs.okhttp.dnsoverhttps)

                // DLNA
                implementation(libs.jupnp.bom.compile)
                implementation(libs.jupnp.support)
                implementation(libs.jupnp.osgi)

                // WebSocket
                implementation(libs.java.websocket)

                // Coroutines
                implementation(libs.kotlinx.coroutines.swing)
            }

        }
        val desktopMain by getting{
            dependencies {
                implementation(compose.desktop.currentOs)
                // Player
                implementation(libs.vlcj)
            }
        }
    }
}


compose.desktop {
    application {
        mainClass = "MainKt"

        buildTypes.release.proguard {
            isEnabled.set(true)
            version.set("7.7.0")
            configurationFiles.from(project.file("src/desktopMain/rules.pro"))
            obfuscate.set(false)
            optimize.set(true)
        }

        jvmArgs("-Dfile.encoding=UTF-8")
        jvmArgs("-Dsun.net.http.allowRestrictedHeaders=true")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "LumenTV"
            packageVersion = libs.versions.app.version.get()
            vendor = "LumenTV Compose"

            modules(
                "java.management",
                "java.net.http",
                "jdk.unsupported",
                "java.naming",
                "java.base",
                "java.sql"
            )
            val dir = project.layout.projectDirectory.dir("src/desktopMain/appResources")
            println(dir)
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/desktopMain/appResources"))
            windows {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/LumenTV-icon-win.ico"))
                dirChooser = true
                upgradeUuid = "161FA5A0-A30B-4568-9E84-B3CD637CC8FE"
                perUserInstall = true
                menu = true
                shortcut = true
            }

            linux {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/LumenTV-icon-256.png"))
            }

            macOS {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/LumenTV-icon-mac.icns"))
            }

        }

    }
}
