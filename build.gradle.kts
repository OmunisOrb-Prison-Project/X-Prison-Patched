plugins {
    id("java")
    alias(libs.plugins.shadow)
}

group = "dev.passarelli.xprison"
version = "1.17.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://nexus.lucko.me/repository/maven-releases/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    implementation(libs.exp4j)
    implementation(libs.xseries)
    implementation(libs.hikaricp) {
        exclude(module = "slf4j-api")
    }
    implementation(libs.postgresql) {
        exclude(module = "checker-qual")
    }
    implementation(libs.worldguard.wrapper)
    implementation(libs.bundles.rtag)

    compileOnly(libs.spigot.api)
    compileOnly(libs.lucko.helper)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.vault.api)
    compileOnly(libs.ultrabackpacks.api)

    testImplementation(libs.spigot.api)
    testImplementation(libs.junit)
    testImplementation(libs.mockito)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

tasks.shadowJar {
    relocate("com.cryptomorin", "${project.group}.dependencies.com.cryptomorin")
    relocate("com.saicone", "${project.group}.dependencies.com.saicone")
    relocate("com.zaxxer", "${project.group}.dependencies.com.zaxxer")
    relocate("net.objecthunter", "${project.group}.dependencies.net.objecthunter")
    relocate("org.codemc", "${project.group}.dependencies.org.codemc")
    relocate("org.postgresql", "${project.group}.dependencies.org.postgresql")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
