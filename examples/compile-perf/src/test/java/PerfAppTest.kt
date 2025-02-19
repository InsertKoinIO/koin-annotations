import org.junit.Test
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.example.components.four.MyModule4
import org.koin.example.components.one.MyModule
import org.koin.example.components.three.MyModule3
import org.koin.example.components.two.MyModule2

import org.koin.ksp.generated.*
import org.koin.mp.KoinPlatform
import kotlin.test.assertEquals

class PerfAppTest {

    @OptIn(KoinInternalApi::class)
    @Test
    fun test_run_all(){
        startKoin {
            printLogger()
            modules(
//            defaultModule,
                MyModule().module,
                MyModule2().module,
                MyModule3().module,
                MyModule4().module
            )
        }
        assertEquals(300*3 + 3*550, KoinPlatform.getKoin().instanceRegistry.instances.size)
        stopKoin()
    }
}