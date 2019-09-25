package com.detroitlabs.quirmy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.scheme.VCard

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<AccountUtils.UserProfile> {

    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_CONTACTS
                ),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
            LoaderManager.getInstance(this).initLoader(0, Bundle.EMPTY, this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                // If request is cancelled, the result arrays are empty.
                // FIXME should check that all grants are granted
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    LoaderManager.getInstance(this).initLoader(0, Bundle.EMPTY, this)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.w(localClassName, "Read Contacts permission denied, unable to get user profile.")
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<AccountUtils.UserProfile> {
        return UserProfileLoader(this)
    }

    override fun onLoadFinished(
        loader: Loader<AccountUtils.UserProfile>,
        data: AccountUtils.UserProfile?
    ) {
        Log.d(localClassName, data!!.toString())

        val phoneNumber =
            if (data.primaryPhoneNumber() != null) data.primaryPhoneNumber()
            else data.possiblePhoneNumbers().firstOrNull()

        val email =
            if (data.primaryEmail() != null) data.primaryEmail()
            else data.possibleEmails().firstOrNull()

        val myVCard = VCard(data.primaryName())
            .setPhoneNumber(phoneNumber)
            .setEmail(email)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val size =
            if (displayMetrics.widthPixels < displayMetrics.heightPixels)
                displayMetrics.widthPixels
            else displayMetrics.heightPixels

        val myBitmap = QRCode.from(myVCard)
            .withCharset("UTF-8")
            .withSize(size, size)
            .bitmap()
        codeImageView.setImageBitmap(myBitmap)
    }

    override fun onLoaderReset(loader: Loader<AccountUtils.UserProfile>) {
        // do nothing
    }
}
