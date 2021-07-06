package com.pavelprojects.filmlibraryproject.ui.vm

import androidx.lifecycle.MutableLiveData

interface NetworkLoadChecker {
    val isNetworkLoading: MutableLiveData<Boolean>
    fun getLoadingStatus() = isNetworkLoading.value
}