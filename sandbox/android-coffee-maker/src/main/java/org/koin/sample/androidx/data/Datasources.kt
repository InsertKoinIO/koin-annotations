package org.koin.sample.androidx.data

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface TaskDatasource

@Single
@Named("local")
class LocalDatasource : TaskDatasource

@Single
@Named("remote")
class RemoteDatasource : TaskDatasource

