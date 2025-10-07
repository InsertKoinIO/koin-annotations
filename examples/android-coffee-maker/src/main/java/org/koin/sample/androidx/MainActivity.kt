package org.koin.sample.androidx

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    private val button : Button by lazy { findViewById(R.id.main_button) }
    private val textView : TextView by lazy { findViewById(R.id.main_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getKoin().declare(MyProvidedComponent())

        setContentView(R.layout.main_activity)
        title = "Android Coffee Maker"

        button.setOnClickListener {
            textView.text = "Coffee Inside !"
        }

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
    }
}