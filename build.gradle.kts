import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("com.google.cloud.tools.jib") version "3.5.1"
}

group = "no.nav.aap"
version = System.getProperty("revision") ?: "0.0.1-SNAPSHOT"
description = "Proxy for å kalle tjenester i FSS"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

repositories {
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    // Required for OpenSAML dependencies used by Apache CXF
    maven {
        url = uri("https://build.shibboleth.net/maven/releases/")
    }
    mavenCentral()
}

configurations {
    implementation {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-jetty")
    }
    // Configuration for CXF code generation
    create("cxfCodegen") {
        extendsFrom(configurations.implementation.get())
    }
}

dependencies {
    // CXF code generation
    add("cxfCodegen", "org.apache.cxf:cxf-codegen-plugin:4.2.0")
    add("cxfCodegen", "org.apache.cxf:cxf-tools-wsdlto-frontend-jaxws:4.2.0")
    add("cxfCodegen", "org.apache.cxf:cxf-tools-wsdlto-databinding-jaxb:4.2.0")
    add("cxfCodegen", "jakarta.xml.ws:jakarta.xml.ws-api:4.0.0")
    add("cxfCodegen", "jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")

    // Apache HTTP Components
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

    // AspectJ
    implementation("org.aspectj:aspectjweaver")

    // Micrometer
    implementation("io.micrometer:context-propagation:1.2.1")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Apache CXF
    implementation("org.apache.cxf:cxf-spring-boot-autoconfigure:4.2.0")
    implementation("org.apache.cxf:cxf-spring-boot-starter-jaxws:4.2.0")
    implementation("org.apache.cxf:cxf-rt-features-logging:4.2.0")
    implementation("org.apache.cxf:cxf-rt-ws-security:4.2.0")

    // SpringDoc OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-common:3.0.1")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

    // Spring Web Services
    implementation("org.springframework.ws:spring-ws-security")
    implementation("org.springframework.ws:spring-ws-core")

    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-webclient")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Hibernate Validator
    implementation("org.hibernate.validator:hibernate-validator")

    // Jakarta
    implementation("jakarta.inject:jakarta.inject-api:2.0.0")

    // Jackson Kotlin Module
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.10.2")

    // NAV Boot
    implementation("no.nav.boot:boot-conditionals:6.0.2")

    // NAV Security
    implementation("no.nav.security:token-validation-spring:6.0.2")
    implementation("no.nav.security:token-client-spring:6.0.2")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.3.10")
    testImplementation("no.nav.security:token-validation-spring-test:6.0.2")
    testImplementation("com.ninja-squad:springmockk:5.0.1")
}

// Configure WSDL to Java code generation using CXF
val generatedSourcesDir = layout.buildDirectory.dir("generated/sources/wsdl2java").get().asFile

// Task to generate code from behandleSakOgAktivitet WSDL
val wsdl2javaBehandleSakOgAktivitet = tasks.register<JavaExec>("wsdl2javaBehandleSak") {
    group = "code generation"
    description = "Generate Java classes from behandleSakOgAktivitet WSDL"

    classpath = configurations.getByName("cxfCodegen")
    mainClass.set("org.apache.cxf.tools.wsdlto.WSDLToJava")

    args = listOf(
        "-autoNameResolution",
        "-exsh",
        "true",
        "-p",
        "no.nav.aap.proxy.arena.generated.behandleSakOgAktivitet",
        "-d",
        generatedSourcesDir.absolutePath,
        "$projectDir/src/main/resources/wsdl/behandleSakOgAktivitet/Binding.wsdl"
    )

    inputs.files("$projectDir/src/main/resources/wsdl/behandleSakOgAktivitet/Binding.wsdl")
    outputs.dir(generatedSourcesDir)

    doFirst {
        generatedSourcesDir.mkdirs()
    }
}

// Task to generate code from oppgave WSDL
val wsdl2javaOppgave = tasks.register<JavaExec>("wsdl2javaOppgave") {
    group = "code generation"
    description = "Generate Java classes from oppgave WSDL"

    classpath = configurations.getByName("cxfCodegen")
    mainClass.set("org.apache.cxf.tools.wsdlto.WSDLToJava")

    args = listOf(
        "-exsh",
        "true",
        "-p",
        "no.nav.aap.proxy.arena.generated.oppgave",
        "-b",
        "$projectDir/src/main/resources/wsdl/oppgave/behandleArbeidOgAktivitetOppgave/bindings.xml",
        "-d",
        generatedSourcesDir.absolutePath,
        "$projectDir/src/main/resources/wsdl/oppgave/no/nav/tjeneste/virksomhet/behandleArbeidOgAktivitetOppgave/v1/Binding.wsdl"
    )

    inputs.files(
        "$projectDir/src/main/resources/wsdl/oppgave/behandleArbeidOgAktivitetOppgave/bindings.xml",
        "$projectDir/src/main/resources/wsdl/oppgave/no/nav/tjeneste/virksomhet/behandleArbeidOgAktivitetOppgave/v1/Binding.wsdl"
    )
    outputs.dir(generatedSourcesDir)

    doFirst {
        generatedSourcesDir.mkdirs()
    }
}

// Task to generate code from sak WSDL
val wsdl2javaSak = tasks.register<JavaExec>("wsdl2javaSak") {
    group = "code generation"
    description = "Generate Java classes from sak WSDL"

    classpath = configurations.getByName("cxfCodegen")
    mainClass.set("org.apache.cxf.tools.wsdlto.WSDLToJava")

    args = listOf(
        "-exsh",
        "true",
        "-p",
        "no.nav.aap.proxy.arena.generated.sak",
        "-d",
        generatedSourcesDir.absolutePath,
        "$projectDir/src/main/resources/wsdl/ytelse/arenasakvedtakservice.wsdl"
    )

    inputs.files("$projectDir/src/main/resources/wsdl/ytelse/arenasakvedtakservice.wsdl")
    outputs.dir(generatedSourcesDir)

    doFirst {
        generatedSourcesDir.mkdirs()
    }
}

// Aggregate task to generate all WSDL code
val generateWsdl = tasks.register("generateWsdl") {
    group = "code generation"
    description = "Generate all Java classes from WSDLs"
    dependsOn(wsdl2javaBehandleSakOgAktivitet, wsdl2javaOppgave, wsdl2javaSak)
}

// Make compilation depend on code generation
tasks.named("compileJava") {
    dependsOn(generateWsdl)
}

tasks.named("compileKotlin") {
    dependsOn(generateWsdl)
}

// Add generated sources to source sets
sourceSets {
    main {
        java {
            srcDir(generatedSourcesDir)
        }
        kotlin {
            srcDir("src/main/kotlin")
        }
    }
    test {
        kotlin {
            srcDir("src/test/kotlin")
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure Spring Boot build info
springBoot {
    buildInfo()
}

// Configure Jib for container image building
jib {
    from {
        image = "gcr.io/distroless/java21"
    }
    to {
        image = System.getenv("IMAGE") ?: "aap-fss-proxy:latest"
        auth {
            username = "x-access-token"
            password = System.getenv("GAR_TOKEN")
        }
    }
    container {
        environment = mapOf("TZ" to "CET")
    }
}
