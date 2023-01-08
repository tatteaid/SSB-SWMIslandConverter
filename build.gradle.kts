plugins {
    id("com.github.johnrengelman.shadow") version("7.1.2")
    id("java")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "me.tatteaid"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()

    // spigot api
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

    // aswm api
    maven("https://repo.rapture.pw/repository/maven-snapshots/")

    // superiorskyblock2 api
    maven("https://repo.bg-software.com/repository/api/")

    // fastasyncworldedit api
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // spigot api
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")

    // aswm api
    compileOnly("com.grinderwolf:slimeworldmanager-api:2.10.0-SNAPSHOT")

    // superiorskyblock2 api
    compileOnly("com.bgsoftware:SuperiorSkyblockAPI:2022.9")

    // fastasyncworldedit api
    implementation(platform("com.intellectualsites.bom:bom-1.18.x:1.20"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
}

tasks.shadowJar {
    archiveBaseName.set("SSB-SWMIslandConverter")
    archiveClassifier.set("")
    archiveVersion.set("1.0.0")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}