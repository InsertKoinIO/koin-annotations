package org.koin.sample.androidx.testviewmodels

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TestViewModel1(
    val serviceA: ServiceA,
    val repositoryA: RepositoryA
) : ViewModel()

@KoinViewModel
class TestViewModel2(
    val serviceB: ServiceB,
    val repositoryB: RepositoryB
) : ViewModel()

@KoinViewModel
class TestViewModel3(
    val serviceC: ServiceC,
    val repositoryC: RepositoryC
) : ViewModel()

@KoinViewModel
class TestViewModel4(
    val useCaseA: UseCaseA,
    val serviceA: ServiceA
) : ViewModel()

@KoinViewModel
class TestViewModel5(
    val useCaseB: UseCaseB,
    val serviceB: ServiceB
) : ViewModel()

@KoinViewModel
class TestViewModel6(
    val useCaseC: UseCaseC,
    val serviceC: ServiceC
) : ViewModel()

@KoinViewModel
class TestViewModel7(
    val managerA: ManagerA,
    val useCaseA: UseCaseA
) : ViewModel()

@KoinViewModel
class TestViewModel8(
    val managerB: ManagerB,
    val useCaseB: UseCaseB
) : ViewModel()

@KoinViewModel
class TestViewModel9(
    val managerC: ManagerC,
    val useCaseC: UseCaseC
) : ViewModel()

@KoinViewModel
class TestViewModel10(
    val serviceA: ServiceA,
    val serviceB: ServiceB,
    val serviceC: ServiceC
) : ViewModel()

@KoinViewModel
class TestViewModel11(
    val repositoryA: RepositoryA,
    val repositoryB: RepositoryB
) : ViewModel()

@KoinViewModel
class TestViewModel12(
    val repositoryB: RepositoryB,
    val repositoryC: RepositoryC
) : ViewModel()

@KoinViewModel
class TestViewModel13(
    val useCaseA: UseCaseA,
    val useCaseB: UseCaseB
) : ViewModel()

@KoinViewModel
class TestViewModel14(
    val useCaseB: UseCaseB,
    val useCaseC: UseCaseC
) : ViewModel()

@KoinViewModel
class TestViewModel15(
    val managerA: ManagerA,
    val managerB: ManagerB
) : ViewModel()

@KoinViewModel
class TestViewModel16(
    val managerB: ManagerB,
    val managerC: ManagerC
) : ViewModel()

@KoinViewModel
class TestViewModel17(
    val serviceA: ServiceA,
    val repositoryB: RepositoryB,
    val useCaseC: UseCaseC
) : ViewModel()

@KoinViewModel
class TestViewModel18(
    val serviceB: ServiceB,
    val repositoryC: RepositoryC,
    val useCaseA: UseCaseA
) : ViewModel()

@KoinViewModel
class TestViewModel19(
    val serviceC: ServiceC,
    val repositoryA: RepositoryA,
    val useCaseB: UseCaseB
) : ViewModel()

@KoinViewModel
class TestViewModel20(
    val managerA: ManagerA,
    val repositoryA: RepositoryA,
    val serviceA: ServiceA
) : ViewModel()

@KoinViewModel
class TestViewModel21(
    val managerB: ManagerB,
    val repositoryB: RepositoryB,
    val serviceB: ServiceB
) : ViewModel()

@KoinViewModel
class TestViewModel22(
    val managerC: ManagerC,
    val repositoryC: RepositoryC,
    val serviceC: ServiceC
) : ViewModel()

@KoinViewModel
class TestViewModel23(
    val useCaseA: UseCaseA,
    val repositoryB: RepositoryB,
    val managerC: ManagerC
) : ViewModel()

@KoinViewModel
class TestViewModel24(
    val useCaseB: UseCaseB,
    val repositoryC: RepositoryC,
    val managerA: ManagerA
) : ViewModel()

@KoinViewModel
class TestViewModel25(
    val useCaseC: UseCaseC,
    val repositoryA: RepositoryA,
    val managerB: ManagerB
) : ViewModel()

@KoinViewModel
class TestViewModel26(
    val serviceA: ServiceA
) : ViewModel()

@KoinViewModel
class TestViewModel27(
    val serviceB: ServiceB
) : ViewModel()

@KoinViewModel
class TestViewModel28(
    val serviceC: ServiceC
) : ViewModel()

@KoinViewModel
class TestViewModel29(
    val repositoryA: RepositoryA
) : ViewModel()

@KoinViewModel
class TestViewModel30(
    val repositoryB: RepositoryB
) : ViewModel()

@KoinViewModel
class TestViewModel31(
    val repositoryC: RepositoryC
) : ViewModel()

@KoinViewModel
class TestViewModel32(
    val useCaseA: UseCaseA
) : ViewModel()

@KoinViewModel
class TestViewModel33(
    val useCaseB: UseCaseB
) : ViewModel()

@KoinViewModel
class TestViewModel34(
    val useCaseC: UseCaseC
) : ViewModel()

@KoinViewModel
class TestViewModel35(
    val managerA: ManagerA
) : ViewModel()

@KoinViewModel
class TestViewModel36(
    val managerB: ManagerB
) : ViewModel()

@KoinViewModel
class TestViewModel37(
    val managerC: ManagerC
) : ViewModel()

@KoinViewModel
class TestViewModel38(
    val serviceA: ServiceA,
    val useCaseA: UseCaseA,
    val managerA: ManagerA
) : ViewModel()

@KoinViewModel
class TestViewModel39(
    val serviceB: ServiceB,
    val useCaseB: UseCaseB,
    val managerB: ManagerB
) : ViewModel()

@KoinViewModel
class TestViewModel40(
    val serviceC: ServiceC,
    val useCaseC: UseCaseC,
    val managerC: ManagerC
) : ViewModel()

@KoinViewModel
class TestViewModel41(
    val repositoryA: RepositoryA,
    val useCaseB: UseCaseB,
    val managerC: ManagerC
) : ViewModel()

@KoinViewModel
class TestViewModel42(
    val repositoryB: RepositoryB,
    val useCaseC: UseCaseC,
    val managerA: ManagerA
) : ViewModel()

@KoinViewModel
class TestViewModel43(
    val repositoryC: RepositoryC,
    val useCaseA: UseCaseA,
    val managerB: ManagerB
) : ViewModel()

@KoinViewModel
class TestViewModel44(
    val serviceA: ServiceA,
    val serviceB: ServiceB
) : ViewModel()

@KoinViewModel
class TestViewModel45(
    val serviceB: ServiceB,
    val serviceC: ServiceC
) : ViewModel()

@KoinViewModel
class TestViewModel46(
    val repositoryA: RepositoryA,
    val useCaseA: UseCaseA
) : ViewModel()

@KoinViewModel
class TestViewModel47(
    val repositoryB: RepositoryB,
    val useCaseB: UseCaseB
) : ViewModel()

@KoinViewModel
class TestViewModel48(
    val repositoryC: RepositoryC,
    val useCaseC: UseCaseC
) : ViewModel()

@KoinViewModel
class TestViewModel49(
    val managerA: ManagerA,
    val serviceB: ServiceB,
    val useCaseC: UseCaseC
) : ViewModel()

@KoinViewModel
class TestViewModel50(
    val managerB: ManagerB,
    val serviceC: ServiceC,
    val useCaseA: UseCaseA
) : ViewModel()
