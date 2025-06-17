package io.availe

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class KReplicaExtension(objects: ObjectFactory, project: Project) {
    val generatePatchable: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val modelJsonSources: ConfigurableFileCollection = project.objects.fileCollection()
}
