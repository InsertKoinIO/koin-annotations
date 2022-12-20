package org.koin.sample.android.library

import org.koin.core.annotation.Single

interface AbstractRepository {
    fun getId() : String
}

@Single
class CommonRepository : AbstractRepository{
    override fun getId(): String = "_ID_"

}