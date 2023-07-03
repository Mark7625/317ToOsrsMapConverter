import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "com.mark"
version = "2.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/org.apache.commons/commons-compress
    implementation("org.apache.commons:commons-compress:1.22")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")
    // https://mvnrepository.com/artifact/io.github.material-ui-swing/DarkStackOverflowTheme
    implementation("io.github.material-ui-swing:DarkStackOverflowTheme:0.0.1-rc3")

    implementation("com.github.weisj:darklaf-core:3.0.2")
    // https://mvnrepository.com/artifact/de.schipplock.gui.swing/lafmanager
    implementation("de.schipplock.gui.swing:lafmanager:0.0.1")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}

tasks {

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = "${project.version}"
            attributes["Main-Class"] = "com.mark.ApplicationKt"
        }

        from(configurations.runtimeClasspath.get().files.map { if (it.isDirectory) it else zipTree(it) })

    }

}