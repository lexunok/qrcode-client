package com.lex.qr

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lex.qr.utils.API
import com.lex.qr.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CustomMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var api: API

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            api.updateFcm(token)
        }

    }
}