package com.example.pictoevents

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pictoevents.OCREngine.IOCREngine
import com.example.pictoevents.OCREngine.OCREngineFreeOCR
import com.example.pictoevents.Repository.Repository
import com.example.pictoevents.UI.TitleDialogFragment
import com.example.pictoevents.Util.FileManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

// Request permission
private const val REQUEST_CODE_PERMISSIONS = 10
// Array of all permissions specified in the manifest file
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_CALENDAR,
    Manifest.permission.WRITE_CALENDAR, Manifest.permission.INTERNET)
//Class variables
private val TAG: String? = MainActivity::class.java.simpleName
private val mainScope = MainScope()

class MainActivity : AppCompatActivity(), LifecycleOwner, TitleDialogFragment.TitleDialogFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create NavHostFragment
        val host : NavHostFragment = supportFragmentManager.findFragmentById(R.id.host_fragment)
                as NavHostFragment? ?: return

        val navController = host.navController
        setupBottomNavMenu(navController)

        //Request  permissions
        if (!allPermissionsGranted()){
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mainScope.launch(Dispatchers.Main){
                if(destination.id == R.id.image_frag || destination.id == R.id.addEventFragment) {
                    bottomNav.visibility = View.GONE
                } else {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }

        createDirectory()
    }
    private fun setupBottomNavMenu(navController : NavController){
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.setupWithNavController(navController)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun createDirectory()
    {
        val fileBase = File(this.externalMediaDirs.first(), "/")
        FileManager.setFileBase(fileBase)
        FileManager.prepareDirectory(
            FileManager.getFileBase())
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        Log.d(TAG, "Callback to main activity")
        val titleDialog = dialog as? TitleDialogFragment
        val selection = titleDialog?.titleSelected
        if (selection != null) {
            Repository.eventTitle = selection
            Log.d(TAG, "This title was registered: ${Repository.eventTitle}")
        }
    }

    fun generateTitleDialog(titleOptions : JSONObject){
        val args = Bundle()
        args.putString("primary", titleOptions.optString("Primary"))
        args.putString("secondary", titleOptions.optString("Secondary"))
        Log.d(TAG, "Title options are: ${titleOptions.optString("Primary")}," +
                "${titleOptions.optString("Secondary")}")
        val dialog = TitleDialogFragment()
        dialog.arguments = args
        dialog.show(supportFragmentManager, TitleDialogFragment.TAG)
    }
}
