import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.4"
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    jacoco
}

group = "net.uoneweb.mapbox"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        authentication {
            create<BasicAuthentication>("basic")
        }
        credentials {
            username = "mapbox"
            password =
                if (project.hasProperty("SDK_REGISTRY_TOKEN")) {
                    project.property("SDK_REGISTRY_TOKEN") as? String
                } else {
                    System.getenv("SDK_REGISTRY_TOKEN")
                }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")
    implementation("org.springframework:spring-web")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-geojson:6.8.0")
    // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
    implementation("org.apache.httpcomponents:httpclient:4.4.1")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.34.0")

    // testing architecture
    testImplementation("com.tngtech.archunit:archunit:1.0.0")

    runtimeOnly("com.h2database:h2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}