package org.koin.sample.androidx.testviewmodels

import org.koin.core.annotation.Single

@Single
class ServiceA {
    fun getData() = "ServiceA Data"
}

@Single
class ServiceB {
    fun getData() = "ServiceB Data"
}

@Single
class ServiceC {
    fun getData() = "ServiceC Data"
}

@Single
class RepositoryA(private val serviceA: ServiceA) {
    fun fetchData() = "RepositoryA: ${serviceA.getData()}"
}

@Single
class RepositoryB(private val serviceB: ServiceB) {
    fun fetchData() = "RepositoryB: ${serviceB.getData()}"
}

@Single
class RepositoryC(private val serviceC: ServiceC) {
    fun fetchData() = "RepositoryC: ${serviceC.getData()}"
}

@Single
class UseCaseA(private val repositoryA: RepositoryA) {
    fun execute() = "UseCaseA: ${repositoryA.fetchData()}"
}

@Single
class UseCaseB(private val repositoryB: RepositoryB) {
    fun execute() = "UseCaseB: ${repositoryB.fetchData()}"
}

@Single
class UseCaseC(private val repositoryC: RepositoryC) {
    fun execute() = "UseCaseC: ${repositoryC.fetchData()}"
}

@Single
class ManagerA(private val useCaseA: UseCaseA) {
    fun manage() = "ManagerA: ${useCaseA.execute()}"
}

@Single
class ManagerB(private val useCaseB: UseCaseB) {
    fun manage() = "ManagerB: ${useCaseB.execute()}"
}

@Single
class ManagerC(private val useCaseC: UseCaseC) {
    fun manage() = "ManagerC: ${useCaseC.execute()}"
}
