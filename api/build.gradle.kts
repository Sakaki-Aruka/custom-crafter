
val version = "5.0.15"

plugins {
    kotlin("jvm") version libs.versions.kotlin
    id("com.gradleup.shadow") version libs.versions.shadowJar
    id("org.jetbrains.dokka") version libs.versions.kotlin
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/groups/public")
    }
}

dependencies {
    compileOnly(libs.paperApi)
    implementation(libs.kotlinStdlib)
    implementation("org.choco-solver:choco-solver:5.0.0-beta.1")

    //test
    implementation("org.jetbrains:annotations:24.1.0")

    testCompileOnly("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.45.0")
    testCompileOnly("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    testCompileOnly(libs.kotlinTestJunit5)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.14.0")
}

tasks.shadowJar {
    archiveFileName = "custom-crafter-${version}.jar"

    // TODO: replace ${project.version} in plugin.yml

    relocate("kotlin", "shade.kotlin.online.aruka")
    relocate("org.chocosolver", "shade.chocosolver.online.aruka")
}

// TODO: write test settings
tasks.test {
    useJUnitPlatform()
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

val apiBaseDir = "src/main/java/io/github/sakaki_aruka/customcrafter"
dokka {
    moduleName.set("Custom_Crafter $version")
    dokkaSourceSets.main {
        includes.from("Module.md")
        sourceRoots.from(projectDir.resolve("src/main"))
        suppressedFiles.from(
            projectDir.resolve("${apiBaseDir}/internal"),
            projectDir.resolve("${apiBaseDir}/CustomCrafter.java"),
            projectDir.resolve("${apiBaseDir}/impl/recipe/CVanillaRecipe.kt")
        )
    }


    dokkaPublications.html {
        outputDirectory.set(rootDir.resolve("docs"))
    }

    pluginsConfiguration.html {
        footerMessage.set("(c) Sakaki-Aruka ")
    }
}
