package ruslan.galiev.myweather.findlocation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ruslan.galiev.myweather.database.SuggestLocationDatabaseDAO

class FindLocationViewModelFactory(
    private val dataSource: SuggestLocationDatabaseDAO,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindLocationViewModel::class.java)) {
            return FindLocationViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}