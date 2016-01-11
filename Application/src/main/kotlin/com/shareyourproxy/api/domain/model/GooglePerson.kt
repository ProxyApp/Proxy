package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.shareyourproxy.util.BaseParcelable

internal data class GooglePerson(val name: GoogleName, val email: String, @SerializedName("image") val profile: GoogleProfileImage, val cover: GoogleCover) : BaseParcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(name, flags)
        dest.writeString(email)
        dest.writeParcelable(profile, flags)
        dest.writeParcelable(cover, flags)
    }

    companion object {
        val CL = GooglePerson::class.java.classLoader
        val CREATOR = object : Parcelable.Creator<GooglePerson> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<GooglePerson?> = arrayOfNulls(size)
        }

        @Suppress("UNCHECKED_CAST")
        private fun readParcel(parcel: Parcel) = GooglePerson(parcel.readParcelable(CL), parcel.readString(), parcel.readParcelable<GoogleProfileImage>(CL), parcel.readParcelable<GoogleCover>(CL))
    }

    internal data class GoogleName(@SerializedName("givenName") val first: String, @SerializedName("familyName") val last: String) : BaseParcelable {
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(first)
            dest.writeString(last)
        }

        companion object {
            val CREATOR = object : Parcelable.Creator<GoogleName> {
                override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
                override fun newArray(size: Int): Array<GoogleName?> = arrayOfNulls(size)
            }

            @Suppress("UNCHECKED_CAST")
            private fun readParcel(parcel: Parcel) = GoogleName(parcel.readString(), parcel.readString())
        }
    }

    internal data class GoogleProfileImage(val url: String, val isDefault: Boolean) : BaseParcelable {
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(url)
            dest.writeValue(isDefault)
        }

        companion object {
            val CL = GoogleProfileImage::class.java.classLoader
            val CREATOR = object : Parcelable.Creator<GoogleProfileImage> {
                override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
                override fun newArray(size: Int): Array<GoogleProfileImage?> = arrayOfNulls(size)
            }

            @Suppress("UNCHECKED_CAST")
            private fun readParcel(parcel: Parcel) = GoogleProfileImage(parcel.readString(), parcel.readValue(CL) as Boolean)
        }
    }

    internal data class GoogleCover(val coverPhoto: GoogleCoverPhoto) : BaseParcelable {
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(coverPhoto, flags)
        }

        companion object {
            val CL = GoogleCover::class.java.classLoader
            val CREATOR = object : Parcelable.Creator<GoogleCover> {
                override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
                override fun newArray(size: Int): Array<GoogleCover?> = arrayOfNulls(size)
            }

            @Suppress("UNCHECKED_CAST")
            private fun readParcel(parcel: Parcel) = GoogleCover(parcel.readParcelable<GoogleCoverPhoto>(CL))
        }
    }

    internal data class GoogleCoverPhoto(val url: String, val width: Int, val height: Int) : BaseParcelable {
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(url)
            dest.writeInt(width)
            dest.writeInt(height)
        }

        companion object {
            val CREATOR = object : Parcelable.Creator<GoogleCoverPhoto> {
                override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
                override fun newArray(size: Int): Array<GoogleCoverPhoto?> = arrayOfNulls(size)
            }

            @Suppress("UNCHECKED_CAST")
            private fun readParcel(parcel: Parcel) = GoogleCoverPhoto(parcel.readString(), parcel.readInt(), parcel.readInt())
        }
    }
}
