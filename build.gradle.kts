import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin ("jvm") version ("1.3.21")
    `java-gradle-plugin`
    // Java plugin is used to compile externalJar file in unit tests
    java
    `build-scan`
    id("com.gradle.plugin-publish") version "0.10.1"
}

project.version = "1.0.0"

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

pluginBundle {
    website = "https://github.com/superbob/uber-java"
    vcsUrl = "https://github.com/superbob/uber-java"
    tags = listOf("uber", "java", "uberjava", "uber-java", "merge")
}

gradlePlugin {
    plugins {
        create("uber-java") {
            id = "eu.superbob.uberjava"
            displayName = "UberJava Plugin"
            description = "Merge multiple Java sources files into a single one"
            implementationClass = "eu.superbob.uberjava.UberJavaPlugin"
        }
    }
}

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

sourceSets {
    create("externalJar")
}

java {
    // Hint to tell gradle and IDE what version of Java to use to run the build.
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(gradleApi())

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.12.0")

    testCompileOnly(gradleTestKit())
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")

    // tests need externalJar sources to be compile first
    testRuntime(sourceSets["externalJar"].output)
}
