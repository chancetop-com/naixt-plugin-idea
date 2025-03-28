import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.25"
  id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.chancetop"
version = "1.0.18-1"

repositories {
  mavenLocal()
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

val coreNgVersion = "9.1.7"
val agentServiceInterfaceVersion = "1.0.3-SNAPSHOT"

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2024.3.3")

    bundledPlugin("com.intellij.java")

    pluginVerifier()
    zipSigner()

    testFramework(TestFrameworkType.Platform)
  }
  testImplementation("junit:junit:4.13.2")

  implementation("core.framework:core-ng:${coreNgVersion}")
  implementation("core.framework:core-ng-api:${coreNgVersion}")
  implementation("com.chancetop:agent-service-interface:${agentServiceInterfaceVersion}")
  implementation("org.javassist:javassist:3.30.2-GA")
}