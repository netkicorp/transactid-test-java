import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.3.50"
    kotlin("plugin.spring") version "1.3.50"
}

group = "com.netki"
version = "1.0.0-beta1"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("com.netki:transactid:1.0.0-beta4")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.bouncycastle:bcprov-jdk15on:1.64")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.64")
    implementation("com.google.protobuf:protobuf-java:3.10.0")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.webjars:jquery:2.2.4")
    implementation("org.webjars:bootstrap:3.3.7")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.apache.logging.log4j:log4j-api:2.13.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.0")
    
    implementation("org.springdoc:springdoc-openapi-core:1.1.44")
    implementation("org.springdoc:springdoc-openapi-ui:1.1.44")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
