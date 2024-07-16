package com.appdev.printitout.ModelClasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LogObject(val id: Long, val task: String, val time: String) : Parcelable
