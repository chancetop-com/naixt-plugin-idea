import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.25"
  id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.chancetop"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
  maven {
    url = uri("https://neowu.github.io/maven-repo/")
    content {
      includeGroupByRegex("core\\.framework.*")
    }
  }
  maven {
    url = uri("https://chancetop-com.github.io/maven-repo/")
    content {
      includeGroupByRegex("com\\.chancetop.*")
    }
  }
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
  }

  patchPluginXml {
    sinceBuild.set("243")
    untilBuild.set("243.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}

val coreNgVersion = "9.1.5"
val agentServiceInterfaceVersion = "1.0.1"

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2024.3.3")

    bundledPlugin("com.intellij.java")

    pluginVerifier()
    zipSigner()

    testFramework(TestFrameworkType.Platform)
  }
  testImplementation("junit:junit:4.13.2")

  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("core.framework:core-ng-api:${coreNgVersion}")
  implementation("com.chancetop:agent-service-interface:${agentServiceInterfaceVersion}")
}