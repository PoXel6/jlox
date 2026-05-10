plugins {
    application
    id("java")
}

group = "me.poxel"
version = "1.0"

application {
    mainClass = "me.poxel.lox.Lox"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}