package com.detroitlabs.quirmy

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

/**
 * A collection of authentication and account connection utilities. With strong inspiration from the Google IO session
 * app.
 * @author Dandré Allison
 */
object AccountUtils {

    /**
     * Interface for interacting with the result of [AccountUtils.getUserProfile].
     */
    class UserProfile {

        /** The primary email address  */
        private var _primary_email: String? = null
        /** The primary name  */
        private var _primary_name: String? = null
        /** The primary phone number  */
        private var _primary_phone_number: String? = null
        /** A list of possible email addresses for the user  */
        private val _possible_emails = ArrayList<String>()
        /** A list of possible names for the user  */
        private val _possible_names = ArrayList<String>()
        /** A list of possible phone numbers for the user  */
        private val _possible_phone_numbers = ArrayList<String>()
        /** A possible photo for the user  */
        private var _possible_photo: Uri? = null

        /**
         * Adds an email address to the list of possible email addresses for the user. Retains information about whether this
         * email address is the primary email address of the user.
         * @param email the possible email address
         * @param is_primary whether the email address is the primary email address
         */
        @JvmOverloads
        fun addPossibleEmail(email: String?, is_primary: Boolean = false) {
            if (email == null) return
            if (is_primary) {
                _primary_email = email
                _possible_emails.add(email)
            } else
                _possible_emails.add(email)
        }

        /**
         * Adds a name to the list of possible names for the user.
         * @param name the possible name
         */
        fun addPossibleName(name: String?) {
            if (name != null) {
                if (_possible_names.isEmpty())
                    _primary_name = name
                _possible_names.add(name)
            }
        }

        /**
         * Adds a phone number to the list of possible phone numbers for the user.
         * @param phone_number the possible phone number
         */
        fun addPossiblePhoneNumber(phone_number: String?) {
            if (phone_number != null) _possible_phone_numbers.add(phone_number)
        }

        /**
         * Adds a phone number to the list of possible phone numbers for the user.  Retains information about whether this
         * phone number is the primary phone number of the user.
         * @param phone_number the possible phone number
         * @param is_primary whether the phone number is teh primary phone number
         */
        fun addPossiblePhoneNumber(phone_number: String?, is_primary: Boolean) {
            if (phone_number == null) return
            if (is_primary) {
                _primary_phone_number = phone_number
                _possible_phone_numbers.add(phone_number)
            } else
                _possible_phone_numbers.add(phone_number)
        }

        /**
         * Sets the possible photo for the user.
         * @param photo the possible photo
         */
        fun addPossiblePhoto(photo: Uri?) {
            if (photo != null) _possible_photo = photo
        }

        /**
         * Retrieves the list of possible email addresses.
         * @return the list of possible email addresses
         */
        fun possibleEmails(): List<String> {
            return _possible_emails
        }

        /**
         * Retrieves the list of possible names.
         * @return the list of possible names
         */
        fun possibleNames(): List<String> {
            return _possible_names
        }

        /**
         * Retrieves the list of possible phone numbers
         * @return the list of possible phone numbers
         */
        fun possiblePhoneNumbers(): List<String> {
            return _possible_phone_numbers
        }

        /**
         * Retrieves the possible photo.
         * @return the possible photo
         */
        fun possiblePhoto(): Uri? {
            return _possible_photo
        }

        /**
         * Retrieves the primary email address.
         * @return the primary email address
         */
        fun primaryEmail(): String? {
            return _primary_email
        }

        /**
         * Retrieves the primary phone number
         * @return the primary phone number
         */
        fun primaryPhoneNumber(): String? {
            return _primary_phone_number
        }

        fun primaryName(): String? {
            return _primary_name
        }


        override fun toString(): String {
            return "UserProfile(" +
                    "_primary_email=$_primary_email, " +
                    "_primary_name=$_primary_name, " +
                    "_primary_phone_number=$_primary_phone_number, " +
                    "_possible_emails=$_possible_emails, " +
                    "_possible_names=$_possible_names, " +
                    "_possible_phone_numbers=$_possible_phone_numbers, " +
                    "_possible_photo=$_possible_photo)"
        }
    }

    /**
     * Retrieves the user profile information.
     * @param context the context from which to retrieve the user profile
     * @return the user profile
     */
    fun getUserProfile(context: Context): UserProfile {
        val content = context.contentResolver
        val cursor = content.query(
            // Retrieves data rows for the device user's 'profile' contact
            Uri.withAppendedPath(
                ContactsContract.Profile.CONTENT_URI,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY
            ),
            ProfileQuery.PROJECTION,

            // Selects only email addresses or names
            ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Contacts.Data.MIMETYPE + "=?",
            arrayOf(
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
            ),

            // Show primary rows first. Note that there won't be a primary email address if the
            // user hasn't specified one.
            ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
        )

        val userProfile = UserProfile()
        var mimeType: String
        while (cursor!!.moveToNext()) {
            mimeType = cursor.getString(ProfileQuery.MIME_TYPE)
            when (mimeType) {
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> userProfile.addPossibleEmail(
                    cursor.getString(ProfileQuery.EMAIL),
                    cursor.getInt(ProfileQuery.IS_PRIMARY_EMAIL) > 0
                )
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> userProfile.addPossibleName(
                    cursor.getString(ProfileQuery.GIVEN_NAME) + " " + cursor.getString(
                        ProfileQuery.FAMILY_NAME
                    )
                )
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> userProfile.addPossiblePhoneNumber(
                    cursor.getString(ProfileQuery.PHONE_NUMBER),
                    cursor.getInt(ProfileQuery.IS_PRIMARY_PHONE_NUMBER) > 0
                )
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE -> userProfile.addPossiblePhoto(
                    Uri.parse(cursor.getString(ProfileQuery.PHOTO))
                )
            }
        }

        cursor.close()

        return userProfile
    }

    /**
     * Contacts user profile query interface.
     */
    private interface ProfileQuery {
        companion object {
            /** The set of columns to extract from the profile query results  */
            val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
                ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
                ContactsContract.Contacts.Data.MIMETYPE
            )

            /** Column index for the email address in the profile query results  */
            const val EMAIL = 0
            /** Column index for the primary email address indicator in the profile query results  */
            const val IS_PRIMARY_EMAIL = 1
            /** Column index for the family name in the profile query results  */
            const val FAMILY_NAME = 2
            /** Column index for the given name in the profile query results  */
            const val GIVEN_NAME = 3
            /** Column index for the phone number in the profile query results  */
            const val PHONE_NUMBER = 4
            /** Column index for the primary phone number in the profile query results  */
            const val IS_PRIMARY_PHONE_NUMBER = 5
            /** Column index for the photo in the profile query results  */
            const val PHOTO = 6
            /** Column index for the MIME type in the profile query results  */
            const val MIME_TYPE = 7
        }
    }
}