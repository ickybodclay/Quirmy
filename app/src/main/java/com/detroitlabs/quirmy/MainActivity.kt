package com.detroitlabs.quirmy

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.scheme.VCard

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<AccountUtils.UserProfile> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        LoaderManager.getInstance(this).initLoader(0, Bundle.EMPTY, this)
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

        val myVCard = VCard(data.primaryName())
            .setPhoneNumber(data.primaryPhoneNumber())
            .setEmail(data.primaryEmail())

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
