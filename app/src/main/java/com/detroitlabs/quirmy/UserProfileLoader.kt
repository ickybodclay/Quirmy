package com.detroitlabs.quirmy

import android.content.Context
import androidx.loader.content.AsyncTaskLoader

/**
 * A custom [android.support.v4.content.Loader] that retrieves the [AccountUtils.UserProfile] asynchronously.
 * @author Dandré Allison
 */
class UserProfileLoader(context: Context) : AsyncTaskLoader<AccountUtils.UserProfile>(context) {

    /** The list of the user's possible email address and name  */
    private var userProfile: AccountUtils.UserProfile? = null

    override fun loadInBackground(): AccountUtils.UserProfile {
        return AccountUtils.getUserProfile(context)
    }

    private fun deliverResult(user_profile: AccountUtils.UserProfile) {
        userProfile = user_profile

        if (isStarted)
        // If the Loader is currently started, we can immediately
        // deliver its results.
            super.deliverResult(user_profile)
    }

    override fun onStartLoading() {
        if (userProfile != null)
        // Delivers the result immediately when it's already available
            deliverResult(userProfile!!)

        if (takeContentChanged() || userProfile == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad()
        }
    }

    override fun onStopLoading() {
        // Attempts to cancel the current load task if possible.
        cancelLoad()
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    override fun onReset() {
        super.onReset()

        // Ensures the loader is stopped
        onStopLoading()

        // Clears the stored list
        if (userProfile != null)
            userProfile = null
    }
}