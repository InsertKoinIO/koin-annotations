package org.koin.compiler.metadata

import org.koin.android.annotation.ActivityRetainedScope
import org.koin.android.annotation.ActivityScope
import org.koin.android.annotation.FragmentScope
import org.koin.core.annotation.ViewModelScope

val VIEWMODEL_SCOPE_ARCHETYPE = DefinitionAnnotation("viewModelScope", "org.koin.viewmodel.scope", ViewModelScope::class)
val ACTIVITY_SCOPE_ARCHETYPE = DefinitionAnnotation("activityScope", "org.koin.androidx.scope.dsl", ActivityScope::class)
val ACTIVITY_RETAINED_SCOPE_ARCHETYPE = DefinitionAnnotation("activityRetainedScope", "org.koin.androidx.scope.dsl", ActivityRetainedScope::class)
val FRAGMENT_SCOPE_ARCHETYPE = DefinitionAnnotation("fragmentScope", "org.koin.androidx.scope.dsl", FragmentScope::class)

val SCOPE_ARCHETYPES_LIST = listOf(VIEWMODEL_SCOPE_ARCHETYPE,ACTIVITY_SCOPE_ARCHETYPE,ACTIVITY_RETAINED_SCOPE_ARCHETYPE,FRAGMENT_SCOPE_ARCHETYPE)
val SCOPE_ARCHETYPES_LIST_NAMES = SCOPE_ARCHETYPES_LIST.map { it.annotationName }