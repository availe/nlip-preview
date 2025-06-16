plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("kreplica") {
            id = "io.availe.kreplica"
            implementationClass = "io.availe.kreplica.plugin.KReplicaPlugin"
        }
    }
}

dependencies {
    implementation(projects.modelKspAnnotations)
    implementation(projects.modelKspProcessor)
    implementation(projects.codegen)
    implementation(projects.codegenRuntime)
}