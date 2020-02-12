import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    groovy
    application
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("nu.studer.jooq")
}

noArg {
    annotation("io.github.sulion.jared.data.DefaultConstructor")
}

group = "io.github.sulion"
version = "0.0.1-SNAPSHOT"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

jooq {
    version = "3.11.11"
    "expenseDb"(sourceSets["main"]) {
        generator {
            name = "org.jooq.codegen.DefaultGenerator"

            generate {
                isDeprecated = false
                isRecords = false
                isImmutablePojos = false
                isFluentSetters = false
            }
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                props {
                    prop {
                        key = "scripts"
                        value = "src/main/resources/db/migration/*"
                    }
                    prop {
                        key = "sort"
                        value = "semantic"
                    }
                    prop {
                        key = "unqualifiedSchema"
                        value = "none"
                    }
                    prop {
                        key = "defaultNameCase"
                        value = "as_is"
                    }
                }
            }

            target {
                packageName = "io.github.sulion.jared.models"
            }
            strategy {
                name = "org.jooq.codegen.DefaultGeneratorStrategy"
            }
        }
    }
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    val ktorVersion = "1.2.2"
    val logbackVersion = "1.2.1"
    val jacksonVersion = "2.9.8"
    val postgresqlVersion = "42.2.8"
    val koinVersion = "2.0.1"
    fun ktor(module: String) = "io.ktor:ktor-$module:$ktorVersion"
    fun ktor() = "io.ktor:ktor:$ktorVersion"
    jooqRuntime("org.jooq:jooq-meta-extensions:3.12.3")
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
    compile("org.jooq:jooq:3.12.3")
    compile("org.jooq:jooq-meta:3.12.3")
    compile("org.jooq:jooq-codegen:3.12.3")
    compile("org.jooq:jooq-meta-extensions:3.12.3")
    compile("com.zaxxer:HikariCP:2.7.8")
    compile("org.flywaydb:flyway-core:6.0.8")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    compile("org.codehaus.groovy:groovy-all:2.5.8")
    testCompile("org.spockframework:spock-core:1.2-groovy-2.5")
    testImplementation("io.mockk:mockk:1.9.3")
    compile("org.koin:koin-ktor:$koinVersion")
    compile("org.koin:koin-logger-slf4j:$koinVersion")
    compile("commons-codec:commons-codec:1.14")
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    dependsOn("generateExpenseDbJooqSchemaSource")
}
tasks {
    compileTestGroovy {
        dependsOn(compileTestKotlin)
        classpath += compileTestKotlin.get().outputs.files
    }
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