package io.availe.utils

import io.availe.Paths
import io.availe.core.Model
import io.availe.core.Module
import io.availe.core.Property
import io.availe.core.Replication
import java.io.File

fun outputDirForModule(module: io.availe.core.Module): File =
    when (module) {
        Module.SHARED -> Paths.sharedRoot
        Module.SERVER -> Paths.serverRoot
    }

fun fieldsForBase(modelParameter: Model): List<Property> =
    modelParameter.properties

fun fieldsForCreate(modelParameter: Model): List<Property> =
    modelParameter.properties.filter { propertyItem ->
        propertyItem.replication == Replication.CREATE || propertyItem.replication == Replication.BOTH
    }

fun fieldsForPatch(modelParameter: Model): List<Property> =
    modelParameter.properties.filter { propertyItem ->
        propertyItem.replication == Replication.PATCH || propertyItem.replication == Replication.BOTH
    }
