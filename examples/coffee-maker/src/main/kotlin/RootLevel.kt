import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.util.UUID

@Module
class RootLevelModule {

    @Single
    fun declareRootLevelData() : RootLevelData = RootLevelData()
}

class RootLevelData() {
    val id : String = UUID.randomUUID().toString()
}

