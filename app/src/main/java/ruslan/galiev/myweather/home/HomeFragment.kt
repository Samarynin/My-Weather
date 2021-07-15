package ruslan.galiev.myweather.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import ruslan.galiev.myweather.R
import ruslan.galiev.myweather.database.WeatherAppDatabase
import ruslan.galiev.myweather.databinding.FragmentHomeBinding
import ruslan.galiev.myweather.setting.Settings
import ruslan.galiev.myweather.setting.TemperatureUnit

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = WeatherAppDatabase.getInstance(application).locationDatabaseDAO

        val viewModelFactory = HomeViewModelFactory(dataSource, application)

        val viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        viewModel.settings = loadSettings()

        binding.viewModel = viewModel

        viewModel.navigateTo.observe(viewLifecycleOwner, { fragmentName ->
            fragmentName?.let {
                when (fragmentName) {
                    "DetailFragment" -> {
                        viewModel.currentLocation.value?.let {
                            findNavController()
                                .navigate(
                                    HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                                        it.latitude.toFloat(),
                                        it.longitude.toFloat(),
                                        it.locationName,
                                        it.country
                                    )
                                )
                        }
                        viewModel.onNavigateComplete()
                    }
                }
            }
        })

        viewModel.notification.observe(
            viewLifecycleOwner,
            { message ->
                message?.let {

                }
            })


        val watchingLocationAdapter = WatchingLocationAdapter(
            WatchingLocationClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                        it.latitude.toFloat(), it.longitude.toFloat(), it.locationName, it.country
                    )
                )
            },
            viewModel.settings.temperatureUnit,
        )
        viewModel.watchingLocations.observe(viewLifecycleOwner, {
            it?.let {
                watchingLocationAdapter.submitList(it)
            }
        })
        binding.listWatchingLocation.adapter = watchingLocationAdapter
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_option_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.navigate_to_find_location_fragment) {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFindLocationFragment())
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadSettings(): Settings {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)

        val temperatureUnit: TemperatureUnit =
            when (sp.getString("temperatureUnit", "Celsius")) {
                "Kelvin" -> TemperatureUnit.Kelvin
                "Fahrenheit" -> TemperatureUnit.Fahrenheit
                else -> TemperatureUnit.Celsius
            }
        return Settings(temperatureUnit)
    }
}
