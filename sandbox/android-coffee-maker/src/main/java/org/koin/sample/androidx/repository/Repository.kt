package org.koin.sample.androidx.repository

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.sample.androidx.data.TaskDatasource

@Single
class Repository(
    @Named(name = "local")
    val local: TaskDatasource,
    @Named(name = "remote")
    val remote: TaskDatasource
)