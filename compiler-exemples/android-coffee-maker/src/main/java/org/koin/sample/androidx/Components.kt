package org.koin.sample.androidx

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface TaskDatasource

@Single
@Named("local")
class LocalDatasource : TaskDatasource

@Single
@Named("remote")
class RemoteDatasource : TaskDatasource

@Single
class Repository(
    @Named("local")
    val local: TaskDatasource,
    @Named("remote")
    val remote: TaskDatasource
)