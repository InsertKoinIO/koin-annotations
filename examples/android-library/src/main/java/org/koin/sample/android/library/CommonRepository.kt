package org.koin.sample.android.library

import org.koin.core.annotation.Single

interface AbstractRepository {
    fun getId() : String
}

@Single
class OtherParam

@Single
class CommonRepository(val lazyParam: Lazy<OtherParam>) : AbstractRepository{
    override fun getId(): String = "_ID_"
}