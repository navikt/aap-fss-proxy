import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.CommandLineArgumentProvider

// Kotlin konfigurasjonen er gitt av pluginen 'aap.conventions' i build-logic
// og settings.gradle.kts

plugins {
    id("aap.conventions")

    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
}

group = "no.nav.aap"
version = System.getProperty("revision") ?: "0.0.1-SNAPSHOT"
description = "Proxy for å kalle tjenester i FSS"

extra["tomcat.version"] = libs.versions.tomcat.get()
extra["freemarker.version"] = "2.3.35"

val cxfCodegen = configurations.create("cxfCodegen") {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
        attribute(
            LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
            objects.named(LibraryElements::class.java, LibraryElements.JAR)
        )
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.EXTERNAL))
    }
}

dependencies {
    // CXF code generation
    add(cxfCodegen.name, libs.cxf.codegen.plugin)
    add(cxfCodegen.name, libs.cxf.wsdlto.frontend.jaxws)
    add(cxfCodegen.name, libs.cxf.wsdlto.databinding.jaxb)
    add(cxfCodegen.name, libs.jakarta.xml.ws.api)
    add(cxfCodegen.name, libs.jakarta.xml.bind.api)

    // Apache HTTP Components
    implementation(libs.httpclient5)
    implementation(libs.httpclient.legacy)

    // AspectJ
    implementation(libs.aspectj.weaver)

    // Micrometer
    implementation(libs.micrometer.context.propagation)
    runtimeOnly(libs.micrometer.registry.prometheus)

    // Apache CXF
    implementation(libs.cxf.spring.boot.autoconfigure)
    implementation(libs.cxf.spring.boot.starter.jaxws)
    implementation(libs.cxf.rt.features.logging)
    implementation(libs.cxf.rt.ws.security)

    // SpringDoc OpenAPI
    implementation(libs.springdoc.openapi.starter.common)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // Spring Web Services
    implementation(libs.spring.ws.security)
    implementation(libs.spring.ws.core)

    // Spring Boot Starters
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.web) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation(libs.spring.boot.starter.jetty)
    implementation(libs.spring.boot.starter.web.services)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.jackson)
    implementation(libs.spring.boot.webclient)

    // Spring GraphQL (client only)
    implementation(libs.spring.graphql)
    annotationProcessor(libs.spring.boot.configuration.processor)


    // Jakarta
    implementation(libs.jakarta.inject)

    // Jackson Kotlin Module
    implementation(libs.jackson.module.kotlin)

    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.slf4j)

    // Logging
    implementation(libs.logstash.logback.encoder)

    // NAV Boot
    implementation(libs.nav.boot.conditionals)

    // NAV Security
    implementation(libs.nav.token.validation.spring)
    implementation(libs.nav.token.client.spring)

    // Test Dependencies
    constraints {
        testRuntimeOnly(libs.bouncycastle.provider)
    }
    testImplementation(libs.nav.token.validation.spring.test)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.springmockk)
}

// Configure WSDL to Java code generation using CXF

// Task to generate code from behandleSakOgAktivitet WSDL
val wsdl2javaBehandleSakOgAktivitet = tasks.register<JavaExec>("wsdl2javaBehandleSak") {
    group = "code generation"
    description = "Generate Java classes from behandleSakOgAktivitet WSDL"

    classpath = cxfCodegen
    mainClass.set("org.apache.cxf.tools.wsdlto.WSDLToJava")

    val wsdlFile =
        rootProject.layout.projectDirectory.file("src/main/resources/wsdl/behandleSakOgAktivitet/Binding.wsdl")
    val outputDir = layout.buildDirectory.dir("generated/sources/wsdl2java/behandleSakOgAktivitet")

    args(
        "-autoNameResolution",
        "-exsh",
        "true",
        "-p",
        "no.nav.aap.proxy.arena.generated.behandleSakOgAktivitet"
    )
    argumentProviders.add(
        CommandLineArgumentProvider {
            listOf("-d", outputDir.get().asFile.absolutePath, wsdlFile.asFile.absolutePath)
        }
    )

    inputs.files(wsdlFile).withPropertyName("wsdl").withPathSensitivity(PathSensitivity.NONE)
    outputs.dir(outputDir)

    doFirst {
        outputDir.get().asFile.mkdirs()
    }
}

// Task to generate code from oppgave WSDL
val wsdl2javaOppgave = tasks.register<JavaExec>("wsdl2javaOppgave") {
    group = "code generation"
    description = "Generate Java classes from oppgave WSDL"

    classpath = cxfCodegen
    mainClass.set("org.apache.cxf.tools.wsdlto.WSDLToJava")

    val bindingsFile = rootProject.layout.projectDirectory.file(
        "src/main/resources/wsdl/oppgave/behandleArbeidOgAktivitetOppgave/bindings.xml"
    )
    val wsdlFile = rootProject.layout.projectDirectory.file(
        "src/main/resources/wsdl/oppgave/no/nav/tjeneste/virksomhet/" +
                "behandleArbeidOgAktivitetOppgave/v1/Binding.wsdl"
    )
    val outputDir = layout.buildDirectory.dir("generated/sources/wsdl2java/oppgave")

    args(
        "-exsh",
        "true",
        "-p",
        "no.nav.aap.proxy.arena.generated.oppgave",
        "-b",
        bindingsFile.asFile.absolutePath
    )
    argumentProviders.add(
        CommandLineArgumentProvider {
            listOf("-d", outputDir.get().asFile.absolutePath, wsdlFile.asFile.absolutePath)
        }
    )

    inputs.files(bindingsFile, wsdlFile)
        .withPropertyName("wsdlAndBindings")
        .withPathSensitivity(PathSensitivity.NONE)
    outputs.dir(outputDir)

    doFirst {
        outputDir.get().asFile.mkdirs()
    }
}

// Task to generate code from sak WSDL
val wsdl2javaSak = tasks.register<JavaExec>("wsdl2javaSak") {
    group = "code generation"
    description = "Generate Java classes from sak WSDL"

    classpath = cxfCodegen
    mainClass.set("org.apache.cxf.tools.wsdlto.WSDLToJava")

    val wsdlFile =
        rootProject.layout.projectDirectory.file("src/main/resources/wsdl/ytelse/arenasakvedtakservice.wsdl")
    val outputDir = layout.buildDirectory.dir("generated/sources/wsdl2java/sak")

    args(
        "-exsh",
        "true",
        "-p",
        "no.nav.aap.proxy.arena.generated.sak"
    )
    argumentProviders.add(
        CommandLineArgumentProvider {
            listOf("-d", outputDir.get().asFile.absolutePath, wsdlFile.asFile.absolutePath)
        }
    )

    inputs.files(wsdlFile).withPropertyName("wsdl").withPathSensitivity(PathSensitivity.NONE)
    outputs.dir(outputDir)

    doFirst {
        outputDir.get().asFile.mkdirs()
    }
}

// Aggregate task to generate all WSDL code
val generateWsdl = tasks.register("generateWsdl") {
    group = "code generation"
    description = "Generate all Java classes from WSDLs"
    dependsOn(wsdl2javaBehandleSakOgAktivitet, wsdl2javaOppgave, wsdl2javaSak)
}

// Wire each generated source directory to compilation through its task output.
sourceSets {
    main {
        java {
            srcDir(wsdl2javaBehandleSakOgAktivitet)
            srcDir(wsdl2javaOppgave)
            srcDir(wsdl2javaSak)
        }
        kotlin {
            srcDir(rootProject.file("src/main/kotlin"))
        }
        resources {
            srcDir(rootProject.file("src/main/resources"))
        }
    }
    test {
        kotlin {
            srcDir(rootProject.file("src/test/kotlin"))
        }
        resources {
            srcDir(rootProject.file("src/test/resources"))
        }
    }
}

// Configure Spring Boot build info
springBoot {
    buildInfo {
        properties {
            excludes.add("time")
        }
    }
}

// Fast filnavn på fatjar, slik at Dockerfile kan referere til en stabil sti
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>().configureEach {
    archiveFileName.set("app.jar")
}
// Tests reference sources relative to the repository root
tasks.withType<Test>().configureEach {
    workingDir = rootProject.projectDir
}
