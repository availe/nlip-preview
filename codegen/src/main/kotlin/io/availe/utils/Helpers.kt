package io.availe.utils

import io.availe.Paths
import io.availe.core.Module
import java.io.File

fun outputDirForModule(module: io.availe.core.Module): File =
    when (module) {
        Module.SHARED -> Paths.sharedRoot
        Module.SERVER -> Paths.serverRoot
    }