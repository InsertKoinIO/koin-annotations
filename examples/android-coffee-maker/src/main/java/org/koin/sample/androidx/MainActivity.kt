package org.koin.sample.androidx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityRetainedScope
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.sample.android.library.MyScope
import org.koin.sample.androidx.app.*
import org.koin.sample.androidx.app.scope.MyActivityOtherScope
import org.koin.sample.androidx.app.scope.MyActivityScope
import org.koin.sample.androidx.app.scope.ScopeViewModel
import org.koin.sample.androidx.data.TaskDatasource
import org.koin.sample.androidx.di.UseContext
import org.koin.sample.androidx.multi.FooB
import org.koin.sample.androidx.testviewmodels.TestScreen1
import org.koin.sample.androidx.testviewmodels.TestScreen2
import org.koin.sample.multi.FooA

class MainActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()

    // inject & ViewModel
    val coffeeViewModel : CoffeeViewModel by viewModel()
    val myPresenter : MyPresenter by inject { parametersOf(this@MainActivity) }
    val todoViewModel : TodoViewModel by viewModel()
    val heater : AndroidHeater by inject()
    val coffeeFactory : AndroidCoffeeMakerTester by inject()
    val scopeVM : ScopeViewModel by viewModel()

    val fooA : FooA by inject()
    val fooB : FooB by inject()

    val myActivityScope : MyActivityScope by inject()
    val myOtherActivityScope : MyActivityOtherScope by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getKoin().declare(MyProvidedComponent())

        title = "Android Coffee Maker"

        // Run existing assertions
        assert(coffeeViewModel.repository.getId() == "_ID_")
        assert(myPresenter.mainActivity == this)
        assert(todoViewModel.repository.local == getKoin().get<TaskDatasource>(named("local")))
        assert(todoViewModel.repository.remote == getKoin().get<TaskDatasource>(named("remote")))
        println("resolved: $heater - $coffeeFactory")

        val scope = getKoin().createScope<MyScope>()
        scope.get<ScopedStuff>()

        println("VM scope data: ${scopeVM.sd.id}")
        println("VM scope other data: ${scopeVM.sod.id}")
        println("VM scope 2nd data: ${scopeVM.ssd.id}")

        println("Activity Scope Data: ${myActivityScope.id}")
        println("Activity Other Scope Data: ${myOtherActivityScope.id}")

        assert(fooB.text != fooA.text)
        assert(fooB.textBase == fooA.textBase)

        getKoin().get<UseContext>()

        // Set up Compose UI
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestViewModelsApp()
                }
            }
        }
    }
}

@Composable
fun TestViewModelsApp() {
    var currentScreen by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when (currentScreen) {
            1 -> {
                TestScreen1()
            }
            2 -> {
                TestScreen2()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                currentScreen = if (currentScreen == 1) 2 else 1
            }
        ) {
            Text(text = if (currentScreen == 1) "Go to Screen 2" else "Go to Screen 1")
        }
    }
}