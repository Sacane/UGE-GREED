plugins {
    java
    application
}

val javaVersion = JavaVersion.VERSION_19


allprojects{
    apply(plugin = "java")
    group = "fr.ramatellier"
    version = "0.0.1"
    repositories {
        mavenCentral()
    }
    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion

    }
    tasks.withType<JavaCompile>{
        options.compilerArgs.addAll(listOf("--enable-preview"))
    }
}

project(":server") {
    apply(plugin = "application")
    application {
        mainClass.set("fr.ramatellier.greed.Application")
    }
    tasks.register("prepareKotlinBuildScriptModel"){}
}