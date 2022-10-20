package org.koin.sample.androidx.data

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface TaskDatasource

@Single
@Named(name = "local")
class LocalDatasource : TaskDatasource

@Single
@Named(name = "remote")
class RemoteDatasource : TaskDatasource

