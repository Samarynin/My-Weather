package ruslan.galiev.myweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import ruslan.galiev.myweather.databinding.ActivityMainBinding
import ruslan.galiev.myweather.network.NetworkConnection

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val networkConnection = NetworkConnection(applicationContext)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawer_layout)

        navController = findNavController(R.id.weatherAppNavigationHostFragment)

        networkConnection.observe(this, { status ->
            if (!status) {
                navController.navigate(R.id.disconnectedFragment)
            } else {
                navController.currentDestination?.let {
                    if (navController.currentDestination!!.id == R.id.disconnectedFragment) {
                        navController.popBackStack()
                    }
                }
            }
        })

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, _: Bundle? ->
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else if (nd.id == R.id.disconnectedFragment) {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

    }


    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.disconnectedFragment) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Request internet",
                Snackbar.LENGTH_INDEFINITE
            ).show()
        } else {
            super.onBackPressed()
        }
    }

}