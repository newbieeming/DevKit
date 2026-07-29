import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.devkit.android.application.compose)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit"

    defaultConfig {
        applicationId = "com.newbieeming.devkit"
        versionCode = 1_000_000
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true  // DevKitApplication 中使用 BuildConfig.DEBUG
    }

    signingConfigs {
        create("me") {
            storeFile = file("../sign/newbieeming.jks")
            storePassword = "newbieeming"
            keyAlias = "newbieeming"
            keyPassword = "newbieeming"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("me")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            signingConfig = signingConfigs.findByName("me")
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.manifestPlaceholders.put("BuildTime", buildTime)
        variant.manifestPlaceholders.put("GitCommitId", gitCommitId)

        if (variant.buildType == "release") {
            variant.outputs.forEach { output ->
                output.outputFileName.set(
                    variant.applicationId.zip(output.versionName) { applicationId, versionName ->
                        "$applicationId-$versionName.apk"
                    },
                )
            }
        }
    }
}

dependencies {
    // ── feature 模块全量接入（由 app 负责组装）───────────────────────────────
    implementation(project(":feature:miccontrol"))
    implementation(project(":feature:audiorecord"))
    implementation(project(":feature:appmanager"))
    implementation(project(":feature:networkspeed"))
    implementation(project(":feature:deviceinfo"))
    implementation(project(":feature:timesync"))
    implementation(project(":feature:stopwatch"))

    // ── 导航 ──────────────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // ── 主题 ──────────────────────────────────────────────────────────────────
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))   // Timber、协程调度器等
    implementation(project(":core:ui"))       // FeatureEntry 接口、FeatureTileScaffold

    // ── 测试 ──────────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


val buildTime = providers.provider {
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX")
        .withZone(ZoneId.of("Asia/Shanghai"))
        .format(Instant.now())
}
val gitCommitId = providers.exec {
    commandLine("git", "rev-parse", "--short=8", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { output ->
    output.trim().ifEmpty { "unknown" }
}
