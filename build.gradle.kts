group = "xyz.srnyx"
version = "0.0.1"
description = "Vanadium"

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://m2.dv8tion.net/releases")
    maven("https://nexus.scarsz.me/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("com.discordsrv:discordsrv:1.25.0")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("net.essentialsx:EssentialsX:2.19.4")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.0.0")
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT"))
            exclude(dependency("com.discordsrv:discordsrv:1.25.0"))
            exclude(dependency("me.clip:placeholderapi:2.11.1"))
            exclude(dependency("net.essentialsx:EssentialsX:2.19.4"))
            exclude(dependency("com.github.LoneDev6:api-itemsadder:3.0.0"))
        }
    }

    jar {
        dependsOn("shadowJar")
        isEnabled = false
    }

    // Set UTF-8 as the encoding
    compileJava {
        options.encoding = "UTF-8"
    }

    // Process Placeholders for the plugin.yml
    processResources {
        filesMatching("**/plugin.yml") {
            expand(rootProject.project.properties)
        }
        // Always re-run this task
        outputs.upToDateWhen { false }
    }

    compileJava {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }

/*    val targetJavaVersion = 17
    java {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion)
        sourceCompatibility = JavaVersion.VERSION_javaVersion
        targetCompatibility = JavaVersion.VERSION_javaVersion
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion(JavaLanguageVersion.of(targetJavaVersion))
        }
    }

    tasks.withType(JavaCompile).configureEach {
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release = targetJavaVersion
        }
    }*/
}