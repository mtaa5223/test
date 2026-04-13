plugins {
    kotlin("jvm") version "2.3.10"
    application
}

val ktorVersion = "3.4.1"
val jooqVersion = "3.19.16"

group = "com.example"
version = "1.0-SNAPSHOT"


application {
    mainClass = "com.example.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.flywaydb:flyway-core:10.6.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.6.0")
    implementation("io.ktor:ktor-server-auth:${ktorVersion}")
    implementation("io.ktor:ktor-server-auth-jwt:${ktorVersion}")
    implementation("com.auth0:jwks-rsa:0.22.1")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    // DB
    implementation("org.jooq:jooq:3.16.6")
    implementation("org.jooq:jooq-meta:3.16.6")
    implementation("org.jooq:jooq-codegen:3.16.6")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
