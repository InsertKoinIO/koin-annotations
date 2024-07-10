package org.koin.sample.androidx.data

import org.koin.core.annotation.Factory

@Factory
class DataConsumer

class MyDataConsumer(val dc : DataConsumer)

@Factory
fun funDataConsumer(dc : DataConsumer) = MyDataConsumer(dc)