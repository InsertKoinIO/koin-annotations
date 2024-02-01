import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.example.components.one.MyModule
import org.koin.example.components.three.MyModule3
import org.koin.example.components.two.MyModule2

import org.koin.ksp.generated.*

class PerfAppTest {

    @Test
    fun test_run_all(){
        startKoin {
            printLogger()
            modules(
//            defaultModule,
                MyModule().module,
                MyModule2().module,
                MyModule3().module
            )
        }
        stopKoin()
    }
}