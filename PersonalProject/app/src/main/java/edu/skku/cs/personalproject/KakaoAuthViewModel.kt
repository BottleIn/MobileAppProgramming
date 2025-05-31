package edu.skku.cs.personalproject

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

class KakaoAuthViewModel : ViewModel() {

    fun login(context: Context, callback: (OAuthToken?, Throwable?) -> Unit) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            callback(token, error)
        }
    }
}
