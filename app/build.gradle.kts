plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mycashbook.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mycashbook.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Test AdMob ID (Replace with real ID for release)
        manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3347511713"

        // Required for Apache POI and large dependency trees
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            // This block forces Gradle to ignore specific files causing conflicts
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                // Crucial exclusions for XMLBeans conflicts
                "org/apache/xmlbeans/xml/stream/**",
                "javax/xml/stream/**",
                // Common conflicts with Google HTTP Client
                "META-INF/io.netty.versions.properties",
                "META-INF/INDEX.LIST"
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Lifecycle components
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime:$lifecycleVersion")

    // Google Auth (Sign In)
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    // Identity is often needed for OneTap, keeping it is good
    implementation("com.google.android.gms:play-services-identity:18.0.1")

    // Network (OkHttp)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okio:okio:3.6.0")

    // Ads + Billing
    implementation("com.google.android.gms:play-services-ads:23.0.0")
    implementation("com.android.billingclient:billing:6.1.0")

    // CSV Handling
    implementation("com.opencsv:opencsv:5.9")

    // --- APACHE POI CONFIGURATION (Excel) ---
    // We rely on standard POI 3.17 but EXCLUDE the conflicting libraries
    implementation("org.apache.poi:poi:3.17")

    implementation("org.apache.poi:poi-ooxml:3.17") {
        // These libraries are already in Android, so excluding them prevents duplicate error
        exclude(group = "stax", module = "stax-api")
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
    }

    // Implementation for schemas
    implementation("org.apache.poi:poi-ooxml-schemas:3.17") {
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
    }

    implementation("com.fasterxml:aalto-xml:1.0.0") // Helper for Android XML

    implementation("androidx.multidex:multidex:2.0.1")

    // Utils
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Security (for EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // --- GOOGLE DRIVE REST API ---
    // These libraries provide: AndroidHttp, Drive, and GsonFactory

    // The main Google API Client for Android
    implementation("com.google.api-client:google-api-client-android:2.2.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava") // Avoid conflict with Android's Guava
    }

    // The Drive API Service specifically
    implementation("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava")
    }

    // HTTP Client for API
    implementation("com.google.http-client:google-http-client-gson:1.43.3") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava")
    }

    implementation("com.google.http-client:google-http-client-android:1.43.3") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava")
    }

    // Add Guava specifically if needed, but usually Android handles it.
    // If you get a "listenablefuture" error, uncomment the line below:
    implementation("com.google.guava:guava:31.1-android")

    // Desugaring (Required for newer Java 8+ features on older devices)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
