plugins {
    id("java")
    id("maven-publish")
}

allprojects {
    group = "com.over64"
    version = "0.0.1"
    repositories {
        jcenter()
        mavenLocal()
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
    options.compilerArgs.add("--enable-preview")
    options.compilerArgs.add("-Xplugin:jScripter --js-src-package=com.over64.greact.dom")
    options.fork()
    options.forkOptions.jvmArgs = listOf("--enable-preview")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
    jvmArgs = listOf("--enable-preview")
}

dependencies {
    implementation("com.over64:jscripter:0.0.1")
    implementation("org.antlr:antlr4-runtime:4.8-1")
    implementation("org.sql2o:sql2o:1.6.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("commons-io:commons-io:2.10.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "greact"
            from(components["java"])
//            versionMapping {
//                usage("java-api") {
//                    fromResolutionOf("runtimeClasspath")
//                }
//                usage("java-runtime") {
//                    fromResolutionResult()
//                }
//            }
            pom {
                name.set("GReact")
                description.set("Java WEB UI library")
//                url.set("http://www.example.com/library")
//                properties.set(mapOf(
//                        "myProp" to "value",
//                        "prop.with.dots" to "anotherValue"
//                ))
//                licenses {
//                    license {
//                        name.set("The Apache License, Version 2.0")
//                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("johnd")
//                        name.set("John Doe")
//                        email.set("john.doe@example.com")
//                    }
//                }
//                scm {
//                    connection.set("scm:git:git://example.com/my-library.git")
//                    developerConnection.set("scm:git:ssh://example.com/my-library.git")
//                    url.set("http://example.com/my-library/")
//                }
            }
        }
    }
}
