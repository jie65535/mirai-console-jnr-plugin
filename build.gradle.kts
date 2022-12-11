plugins {
    val kotlinVersion = "1.6.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.2"
}

group = "top.jie65535"
version = "1.2.1"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
