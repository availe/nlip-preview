package io.availe.utils

import io.availe.models.Model
import io.availe.models.Property
import io.availe.models.Replication

fun fieldsForBase(model: Model): List<Property> = model.properties

fun fieldsForCreate(model: Model): List<Property> =
    model.properties.filter { it.replication == Replication.CREATE || it.replication == Replication.BOTH }

fun fieldsForPatch(model: Model): List<Property> =
    model.properties.filter { it.replication == Replication.PATCH || it.replication == Replication.BOTH }

fun fieldsForInterface(model: Model): List<Property> =
    model.properties.filter { it.replication == Replication.BOTH }
