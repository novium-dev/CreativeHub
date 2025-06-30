import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("com.gradleup.shadow") version "8.3.6"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "world.novium"
version = "1.0-SNAPSHOT"

val mcVersion = "1.21.4"
val commandAPIVersion = "10.0.1"

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev
        .ReobfArtifactConfiguration.MOJANG_PRODUCTION
}


repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

val saduVersion = "2.3.2"

val shadowDependencies = listOf(
    "dev.jorel:commandapi-bukkit-shade-mojang-mapped:$commandAPIVersion",
    "dev.triumphteam:triumph-gui:3.1.11",
    "io.javalin:javalin:6.6.0",
    "com.github.ben-manes.caffeine:caffeine:3.2.0",
    "com.google.inject:guice:7.0.0",
    "org.projectlombok:lombok:1.18.36",
    "org.atteo.classindex:classindex:3.13",
    "de.chojo.sadu:sadu-mariadb:$saduVersion",
    "de.chojo.sadu:sadu-datasource:$saduVersion",
    "de.chojo.sadu:sadu-queries:$saduVersion",
    "de.chojo.sadu:sadu-updater:$saduVersion",
    "com.zaxxer:HikariCP:6.3.0",
    "org.mariadb.jdbc:mariadb-java-client:3.5.3"
)

dependencies {
    paperweight.paperDevBundle("$mcVersion-R0.1-SNAPSHOT")

    shadowDependencies.forEach { dependency ->
        paperLibrary(dependency)
    }

    annotationProcessor("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.atteo.classindex:classindex:3.13")

    implementation(platform("com.intellectualsites.bom:bom-newest:1.52"))
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit") { isTransitive = false }
    compileOnly("net.luckperms:api:5.4")
}


tasks {
    build {
        dependsOn("shadowJar")
        dependsOn(reobfJar)
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        configurations = listOf(project.configurations.shadow.get())
        archiveFileName.set("${project.name}.jar")
    }

    runServer {
        minecraftVersion(mcVersion)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

paper {
    main = "world.novium.creative.CreativePlugin"
    loader = "world.novium.creative.DependencyLoader"
    apiVersion = "1.19"
    authors = listOf("InvalidJoker")
    generateLibrariesJson = true

    serverDependencies {
        register("PlotSquared") {
            required = true
        }
    }
}