package org.koin.sample.androidx.testviewmodels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun TestScreen2() {
    val viewModel26: TestViewModel26 = koinViewModel()
    val viewModel27: TestViewModel27 = koinViewModel()
    val viewModel28: TestViewModel28 = koinViewModel()
    val viewModel29: TestViewModel29 = koinViewModel()
    val viewModel30: TestViewModel30 = koinViewModel()
    val viewModel31: TestViewModel31 = koinViewModel()
    val viewModel32: TestViewModel32 = koinViewModel()
    val viewModel33: TestViewModel33 = koinViewModel()
    val viewModel34: TestViewModel34 = koinViewModel()
    val viewModel35: TestViewModel35 = koinViewModel()
    val viewModel36: TestViewModel36 = koinViewModel()
    val viewModel37: TestViewModel37 = koinViewModel()
    val viewModel38: TestViewModel38 = koinViewModel()
    val viewModel39: TestViewModel39 = koinViewModel()
    val viewModel40: TestViewModel40 = koinViewModel()
    val viewModel41: TestViewModel41 = koinViewModel()
    val viewModel42: TestViewModel42 = koinViewModel()
    val viewModel43: TestViewModel43 = koinViewModel()
    val viewModel44: TestViewModel44 = koinViewModel()
    val viewModel45: TestViewModel45 = koinViewModel()
    val viewModel46: TestViewModel46 = koinViewModel()
    val viewModel47: TestViewModel47 = koinViewModel()
    val viewModel48: TestViewModel48 = koinViewModel()
    val viewModel49: TestViewModel49 = koinViewModel()
    val viewModel50: TestViewModel50 = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Test Screen 2 - 25 ViewModels",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("ViewModel26: ${viewModel26.serviceA.getData()}")
        Text("ViewModel27: ${viewModel27.serviceB.getData()}")
        Text("ViewModel28: ${viewModel28.serviceC.getData()}")
        Text("ViewModel29: ${viewModel29.repositoryA.fetchData()}")
        Text("ViewModel30: ${viewModel30.repositoryB.fetchData()}")
        Text("ViewModel31: ${viewModel31.repositoryC.fetchData()}")
        Text("ViewModel32: ${viewModel32.useCaseA.execute()}")
        Text("ViewModel33: ${viewModel33.useCaseB.execute()}")
        Text("ViewModel34: ${viewModel34.useCaseC.execute()}")
        Text("ViewModel35: ${viewModel35.managerA.manage()}")
        Text("ViewModel36: ${viewModel36.managerB.manage()}")
        Text("ViewModel37: ${viewModel37.managerC.manage()}")
        Text("ViewModel38: ${viewModel38.serviceA.getData()}")
        Text("ViewModel39: ${viewModel39.serviceB.getData()}")
        Text("ViewModel40: ${viewModel40.serviceC.getData()}")
        Text("ViewModel41: ${viewModel41.repositoryA.fetchData()}")
        Text("ViewModel42: ${viewModel42.repositoryB.fetchData()}")
        Text("ViewModel43: ${viewModel43.repositoryC.fetchData()}")
        Text("ViewModel44: ${viewModel44.serviceA.getData()}")
        Text("ViewModel45: ${viewModel45.serviceB.getData()}")
        Text("ViewModel46: ${viewModel46.repositoryA.fetchData()}")
        Text("ViewModel47: ${viewModel47.repositoryB.fetchData()}")
        Text("ViewModel48: ${viewModel48.repositoryC.fetchData()}")
        Text("ViewModel49: ${viewModel49.managerA.manage()}")
        Text("ViewModel50: ${viewModel50.managerB.manage()}")
    }
}
