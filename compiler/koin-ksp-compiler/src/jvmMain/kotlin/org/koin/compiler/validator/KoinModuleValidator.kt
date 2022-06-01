package org.koin.compiler.validator

import com.google.devtools.ksp.processing.KSPLogger
import org.koin.compiler.scanner.ModuleMap

class KoinModuleValidator(private val logger: KSPLogger) {

    fun validate(moduleMap: ModuleMap) {
        checkConflictedComponentScanExists(moduleMap)
    }

    private fun checkConflictedComponentScanExists(moduleMap: ModuleMap) {
        logger.logging("check conflicted ComponentScan module exists ...")
        for ((key, value) in moduleMap) {
            val componentScanningModules = value.filter { it.componentScan != null }
            check(componentScanningModules.size <= 1) {
                val moduleNames = componentScanningModules.joinToString { "${it.packageName}.${it.name}" }
                "ComponentScan is conflicted in key $key. Please resolve $moduleNames"
            }
        }
    }
}