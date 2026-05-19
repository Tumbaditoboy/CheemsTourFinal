import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "mx.itson.cheemstour"
    compileSdk = 36

    defaultConfig {
        applicationId = "mx.itson.cheemstour"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        val propsFile = rootProject.file("local.properties")

        var weatherKey = "\"\""
        var mapsKey = ""

        if (propsFile.exists()) {
            val props = Properties()

            propsFile.inputStream().use { stream ->
                props.load(stream)
            }


            weatherKey = "\"${props.getProperty("OPENWEATHER_API_KEY") ?: ""}\""

            mapsKey = props.getProperty("GOOGLE_MAPS_API_KEY") ?: ""
        }


        buildConfigField(
            "String",
            "WEATHER_API_KEY",
            weatherKey
        )


        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.gson.converter)
    implementation(libs.google.maps)
    implementation(libs.picasso)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
