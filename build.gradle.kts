import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    groovy
    application
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

noArg {
    annotation("io.github.sulion.jared.data.DefaultConstructor")
}

group = "io.github.sulion"
version = "0.0.1-SNAPSHOT"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "1.2.2"
    val logbackVersion = "1.2.1"
    val jacksonVersion = "2.9.8"
    fun ktor(module: String) = "io.ktor:ktor-$module:$ktorVersion"
    fun ktor() = "io.ktor:ktor:$ktorVersion"
    compile("org.codehaus.groovy:groovy-all:2.3.11")
    implementation(kotlin("stdlib"))
    testCompile("junit", "junit", "4.12")
    compile(ktor())
    compile(ktor("server-netty"))
    compile("org.telegram:telegrambots:4.3.1")
    implementation(ktor("jackson"))
    compile("ch.qos.logback:logback-classic:$logbackVersion")
    compile("org.apache.commons:commons-lang3:3.9")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    compile("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    compile("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    compile("org.codehaus.groovy:groovy-all:2.5.8")
    testCompile("org.spockframework:spock-core:1.2-groovy-2.5")
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}