package edu.skku.cs.personalproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

class StartActivity : AppCompatActivity() {
    companion object {
        const val TAG = "KakaoAuthViewModel"
    }

    private lateinit var btnKakaoLogin: ImageButton
    private lateinit var nicknameTextView: TextView
    private val kakaoAuthViewModel: KakaoAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        nicknameTextView = findViewById(R.id.nickname)
        btnKakaoLogin = findViewById(R.id.btn_kakao_login)

        btnKakaoLogin.setOnClickListener {
            loginWithKakao()
        }

        val keyHash = com.kakao.sdk.common.util.Utility.getKeyHash(this)
        Log.d("Hash", keyHash)
    }

    private fun loginWithKakao() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                UserApiClient.instance.me { user, error ->
                    val userNickname = user?.kakaoAccount?.profile?.nickname
                    nicknameTextView.text = "닉네임: $userNickname"
                    val intent = Intent(this, SelectActivity::class.java)
                    intent.putExtra("nickname", userNickname)
                    startActivity(intent)
                    finish()
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    if (error is com.kakao.sdk.common.model.ClientError && error.reason == com.kakao.sdk.common.model.ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    kakaoAuthViewModel.login(this, callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    UserApiClient.instance.me { user, error ->
                        val userNickname = user?.kakaoAccount?.profile?.nickname
                        nicknameTextView.text = "닉네임: $userNickname"
                        val intent = Intent(this, SelectActivity::class.java)
                        intent.putExtra("nickname", userNickname)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        } else {
            kakaoAuthViewModel.login(this, callback)
        }
    }
}
