package edu.skku.cs.personalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SelectActivity : AppCompatActivity() {
    private lateinit var btnToMain: Button
    private lateinit var btnToDictionary: Button
    private lateinit var tvWelcome: TextView
    private lateinit var nickname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        nickname = intent.getStringExtra("nickname") ?: "Unknown"

        tvWelcome = findViewById(R.id.tvWelcome)
        tvWelcome.text = "환영합니다, $nickname 님!"

        btnToMain = findViewById(R.id.btn_to_main)
        btnToDictionary = findViewById(R.id.btn_to_dictionary)

        btnToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("nickname", nickname)
            startActivity(intent)
        }

        btnToDictionary.setOnClickListener {
            val intent = Intent(this, WordSearchActivity::class.java)
            intent.putExtra("nickname", nickname)
            startActivity(intent)
        }
    }
}
