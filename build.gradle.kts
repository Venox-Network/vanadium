description = "Vanadium"
version = "0.5.2"
group = "network.venox"

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://m2.dv8tion.net/releases")
    maven("https://nexus.scarsz.me/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
    compileOnly("com.discordsrv:discordsrv:1.25.0")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("net.essentialsx:EssentialsX:2.19.4")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.0.0")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

application.mainClass.set("network.venox.Main")

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        filesMatching("**/plugin.yml") {
            expand(rootProject.project.properties)
        }
    }
}
