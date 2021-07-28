package com.example.lingo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.type.LatLng

data class AvailableUser(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val image: String = "",
    val isAvailable: Boolean = true,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(phoneNumber)
        parcel.writeString(image)
        parcel.writeByte(if (isAvailable) 1 else 0)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<AvailableUser> {
        override fun createFromParcel(parcel: Parcel): AvailableUser {
            return AvailableUser(parcel)
        }

        override fun newArray(size: Int): Array<AvailableUser?> {
            return arrayOfNulls(size)
        }
    }
}