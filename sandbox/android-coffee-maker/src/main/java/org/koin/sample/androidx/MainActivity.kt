package org.koin.sample.androidx

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.sample.android.library.MyScope
import org.koin.sample.androidx.app.*
import org.koin.sample.androidx.data.TaskDatasource

class MainActivity : AppCompatActivity() {

    // inject & ViewModel
    val coffeeViewModel : CoffeeViewModel by viewModel()
    val myPresenter : MyPresenter by inject { parametersOf(this@MainActivity) }
    val todoViewModel : TodoViewModel by viewModel()
    val heater : AndroidHeater by inject()
    val coffeeFactory : AndroidCoffeeMakerTester by inject()

    private val button : Button by lazy { findViewById(R.id.main_button) }
    private val textView : TextView by lazy { findViewById(R.id.main_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }
}