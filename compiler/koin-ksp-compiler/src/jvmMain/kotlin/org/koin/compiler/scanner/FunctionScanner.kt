package org.koin.compiler.scanner

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import org.koin.compiler.metadata.*

abstract class FunctionScanner(
    private val isModuleFunction : Boolean
) {

    fun declareDefinition(
        annotationName: String,
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        ksFunctionDeclaration: KSFunctionDeclaration,
        annotations: Map<String, KSAnnotation> = emptyMap()
    ): KoinMetaData.Definition.FunctionDefinition? {
        val allBindings = declaredBindings(annotation) ?: emptyList()
        val functionParameters = ksFunctionDeclaration.parameters.getConstructorParameters()

        return when (annotationName) {
            SINGLE.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, functionName, functionParameters, allBindings)
            }
            SINGLETON.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, functionName, functionParameters, allBindings)
            }
            FACTORY.annotationName -> {
                createDefinition(FACTORY,packageName,qualifier,functionName,functionParameters,allBindings)
            }
            KOIN_VIEWMODEL.annotationName -> {
                createDefinition(KOIN_VIEWMODEL,packageName,qualifier,functionName,functionParameters,allBindings)
            }
            KOIN_WORKER.annotationName -> {
                createDefinition(KOIN_WORKER,packageName,qualifier,functionName,functionParameters,allBindings)
            }
            SCOPE.annotationName -> {
                val scopeData : KoinMetaData.Scope = annotation.arguments.getScope()
                val extraAnnotation = getExtraScopeAnnotation(annotations)
                createDefinition(extraAnnotation ?: SCOPE,packageName,qualifier,functionName,functionParameters,allBindings,scope = scopeData)
            }
            else -> null
        }
    }

    private fun createSingleDefinition(
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        functionParameters: List<KoinMetaData.ConstructorParameter>,
        allBindings: List<KSDeclaration>
    ): KoinMetaData.Definition.FunctionDefinition {
        val createdAtStart: Boolean =
            annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean?
                ?: false
        return createDefinition(
            SINGLE,
            packageName,
            qualifier,
            functionName,
            functionParameters,
            allBindings,
            isCreatedAtStart = createdAtStart
        )
    }

    private fun createDefinition(
        keyword : DefinitionAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        parameters: List<KoinMetaData.ConstructorParameter>?,
        allBindings: List<KSDeclaration>,
        isCreatedAtStart : Boolean? = null,
        scope: KoinMetaData.Scope? = null,
    ) = KoinMetaData.Definition.FunctionDefinition(
        packageName = packageName,
        qualifier = qualifier,
        isCreatedAtStart = isCreatedAtStart,
        functionName = functionName,
        parameters = parameters ?: emptyList(),
        bindings = allBindings,
        keyword = keyword,
        scope = scope
    ).apply { isClassFunction = isModuleFunction }

}