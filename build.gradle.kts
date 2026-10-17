import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    kotlin("jvm") version "2.4.20"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.esmpfun"
version = "0.1.0"

// ============================================================================
// Three builds from one source tree, one per supported Minecraft line:
//   ./gradlew build                  MC 1.21.x      MobBuckets-<ver>.jar
//   ./gradlew build -PmcVariant=26   MC 26.0-26.2   MobBuckets-<ver>-mc26.jar
//   ./gradlew build -PmcVariant=263  MC 26.3+       MobBuckets-<ver>-mc263.jar
// ============================================================================
data class Variant(
    val paperApi: String,
    val apiVersion: String,
    val jdk: Int,
    val suffix: String,
)

val mcVariant: String = findProperty("mcVariant") as String? ?: "1.21"
val variant: Variant = when (mcVariant) {
    "1.21" -> Variant("1.21.1-R0.1-SNAPSHOT", "1.21", 21, "")
    "26" -> Variant("26.1.2.build.+", "26.1", 25, "-mc26")
    "263" -> Variant("26.3.build.178-stable", "26.3", 25, "-mc263")
    else -> error("Unknown mcVariant '$mcVariant'. Use 1.21, 26 or 263.")
}
val isJava25: Boolean = variant.jdk == 25

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${variant.paperApi}")

    implementation(kotlin("stdlib"))

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:${variant.paperApi}")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(variant.jdk))
    }
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
}

// Paper 26.x publishes Java 25 variants only, and Gradle 9 enforces the attribute.
if (isJava25) {
    configurations.matching {
        it.name in setOf("compileClasspath", "runtimeClasspath", "testCompileClasspath", "testRuntimeClasspath")
    }.configureEach {
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
        }
    }
}

tasks {
    shadowJar {
        archiveClassifier.set(variant.suffix.removePrefix("-"))
        exclude("META-INF/maven/**")
    }

    jar { enabled = false }

    build {
        dependsOn(shadowJar)
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to version,
        "apiVersion" to variant.apiVersion,
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand(props) }
}

tasks.register("printVariant") {
    doLast {
        println("Mob Buckets build for MC $mcVariant (Paper API ${variant.paperApi}, JDK ${variant.jdk})")
    }
}
