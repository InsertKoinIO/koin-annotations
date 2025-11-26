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
fun TestScreen1() {
    val viewModel1: TestViewModel1 = koinViewModel()
    val viewModel2: TestViewModel2 = koinViewModel()
    val viewModel3: TestViewModel3 = koinViewModel()
    val viewModel4: TestViewModel4 = koinViewModel()
    val viewModel5: TestViewModel5 = koinViewModel()
    val viewModel6: TestViewModel6 = koinViewModel()
    val viewModel7: TestViewModel7 = koinViewModel()
    val viewModel8: TestViewModel8 = koinViewModel()
    val viewModel9: TestViewModel9 = koinViewModel()
    val viewModel10: TestViewModel10 = koinViewModel()
    val viewModel11: TestViewModel11 = koinViewModel()
    val viewModel12: TestViewModel12 = koinViewModel()
    val viewModel13: TestViewModel13 = koinViewModel()
    val viewModel14: TestViewModel14 = koinViewModel()
    val viewModel15: TestViewModel15 = koinViewModel()
    val viewModel16: TestViewModel16 = koinViewModel()
    val viewModel17: TestViewModel17 = koinViewModel()
    val viewModel18: TestViewModel18 = koinViewModel()
    val viewModel19: TestViewModel19 = koinViewModel()
    val viewModel20: TestViewModel20 = koinViewModel()
    val viewModel21: TestViewModel21 = koinViewModel()
    val viewModel22: TestViewModel22 = koinViewModel()
    val viewModel23: TestViewModel23 = koinViewModel()
    val viewModel24: TestViewModel24 = koinViewModel()
    val viewModel25: TestViewModel25 = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Test Screen 1 - 25 ViewModels",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("ViewModel1: ${viewModel1.serviceA.getData()}")
        Text("ViewModel2: ${viewModel2.serviceB.getData()}")
        Text("ViewModel3: ${viewModel3.serviceC.getData()}")
        Text("ViewModel4: ${viewModel4.useCaseA.execute()}")
        Text("ViewModel5: ${viewModel5.useCaseB.execute()}")
        Text("ViewModel6: ${viewModel6.useCaseC.execute()}")
        Text("ViewModel7: ${viewModel7.managerA.manage()}")
        Text("ViewModel8: ${viewModel8.managerB.manage()}")
        Text("ViewModel9: ${viewModel9.managerC.manage()}")
        Text("ViewModel10: ${viewModel10.serviceA.getData()}")
        Text("ViewModel11: ${viewModel11.repositoryA.fetchData()}")
        Text("ViewModel12: ${viewModel12.repositoryB.fetchData()}")
        Text("ViewModel13: ${viewModel13.useCaseA.execute()}")
        Text("ViewModel14: ${viewModel14.useCaseB.execute()}")
        Text("ViewModel15: ${viewModel15.managerA.manage()}")
        Text("ViewModel16: ${viewModel16.managerB.manage()}")
        Text("ViewModel17: ${viewModel17.serviceA.getData()}")
        Text("ViewModel18: ${viewModel18.serviceB.getData()}")
        Text("ViewModel19: ${viewModel19.serviceC.getData()}")
        Text("ViewModel20: ${viewModel20.managerA.manage()}")
        Text("ViewModel21: ${viewModel21.managerB.manage()}")
        Text("ViewModel22: ${viewModel22.managerC.manage()}")
        Text("ViewModel23: ${viewModel23.useCaseA.execute()}")
        Text("ViewModel24: ${viewModel24.useCaseB.execute()}")
        Text("ViewModel25: ${viewModel25.useCaseC.execute()}")
    }
}
