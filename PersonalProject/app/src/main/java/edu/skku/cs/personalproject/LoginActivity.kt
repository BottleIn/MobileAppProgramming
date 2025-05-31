package edu.skku.cs.personalproject

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class LoginActivity : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "9ec9694b688f469179c17c23f4f671e1")
    }
}