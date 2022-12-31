plugins {
    id("java")
}

group = "me.tatteaid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.bg-software.com/repository/nms/")
    maven("https://repo.bg-software.com/repository/api/")
    maven("https://repo.bg-software.com/repository/common/")
    maven("https://repo.bg-software.com/repository/public-libs/")
}

dependencies {
    compileOnly("com.bgsoftware:SuperiorSkyblockAPI:2022.8.1")

    // Spigot jars
    compileOnly("org.spigotmc:v1_8_R3-Taco:latest")
}