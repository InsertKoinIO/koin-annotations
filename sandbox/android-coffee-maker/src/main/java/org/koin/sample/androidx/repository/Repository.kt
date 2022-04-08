package org.koin.sample.androidx.repository

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.sample.androidx.data.TaskDatasource

@Single
class Repository(
    @Named("local")
    val local: TaskDatasource,
    @Named("remote")
    val remote: TaskDatasource
)