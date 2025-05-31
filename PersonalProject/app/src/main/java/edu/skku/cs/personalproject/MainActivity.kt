package edu.skku.cs.personalproject

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val history = mutableListOf<String>()
    private val apiKey = "A4429A1B2FDEFC1ED54A3DE39BE2C8F3"
    private var score = 0

    private lateinit var tvGameInfo: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvDefinition: TextView
    private lateinit var tvTimer: TextView
    private lateinit var etUserInput: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnRestart: Button
    private lateinit var btnQuit: Button
    private lateinit var btnRank: Button
    private lateinit var llHistory: LinearLayout
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nickname = intent.getStringExtra("nickname")
        val nicknameTextView: TextView = findViewById(R.id.tvNickname)
        nicknameTextView.text = "닉네임: $nickname"

        tvGameInfo = findViewById(R.id.tvGameInfo)
        tvScore = findViewById(R.id.tvScore)
        tvDefinition = findViewById(R.id.tvDefinition)
        tvTimer = findViewById(R.id.tvTimer)
        etUserInput = findViewById(R.id.etUserInput)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnRestart = findViewById(R.id.btnRestart)
        btnQuit = findViewById(R.id.btnQuit)
        btnRank = findViewById(R.id.btnRank)
        llHistory = findViewById(R.id.llHistory)

        btnRank.isEnabled = false

        etUserInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                etUserInput.setTextColor(Color.BLACK)
            }
        })

        btnSubmit.setOnClickListener {
            val userInput = etUserInput.text.toString()
            handleUserInput(userInput)
        }

        btnRestart.setOnClickListener {
            restartGame()
        }

        btnQuit.setOnClickListener {
            quitGame()
            btnRank.isEnabled = true
        }

        btnRank.setOnClickListener {
            val nickname = nicknameTextView.text.toString().replace("닉네임: ", "")
            val intent = Intent(this, RankActivity::class.java)
            intent.putExtra("nickname", nickname)
            intent.putExtra("score", score)
            startActivity(intent)
        }

        startTimer()
    }

    private fun handleUserInput(query: String) {
        if (query.isBlank()) {
            showToast("단어를 입력하세요.")
            etUserInput.setTextColor(Color.RED)
            return
        }
        if (query.length < 2) {
            showToast("2 글자 이상의 단어만 가능합니다.")
            etUserInput.setTextColor(Color.RED)
            return
        }
        if (query in history) {
            showToast("이미 사용한 단어입니다.")
            etUserInput.setTextColor(Color.RED)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val (definition, isNoun) = checkExists(query)
            withContext(Dispatchers.Main) {
                if (definition.isEmpty()) {
                    showToast("유효한 단어를 입력하세요.")
                    etUserInput.setTextColor(Color.RED)
                    return@withContext
                }

                if (!isNoun) {
                    showToast("입력한 단어가 유효하지 않거나 명사가 아닙니다.")
                    etUserInput.setTextColor(Color.RED)
                    return@withContext
                }

                history.add(query)
                updateScore()
                etUserInput.text.clear()  // 입력 성공 후 clear 하기
                resetTimer()  // 단어 입력에 성공하면 타이머를 재설정

                tvGameInfo.text = query
                tvDefinition.text = "뜻: $definition"
                addWordToHistory(query, definition)

                if (score >= 100) {  // 100점으로 변경
                    tvGameInfo.text = "축하합니다! 당신의 승리입니다!"
                    saveScore(findViewById<TextView>(R.id.tvNickname).text.toString().replace("닉네임: ", ""), 100)
                    resetGame()
                    return@withContext
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val nextWord = findWord(query.last().toString())
                    val nextWordDefinition = checkExists(nextWord).first
                    withContext(Dispatchers.Main) {
                        if (nextWord.isEmpty()) {
                            tvGameInfo.text = "당신의 승리!"
                            saveScore(findViewById<TextView>(R.id.tvNickname).text.toString().replace("닉네임: ", ""), 100)
                            resetGame()
                        } else {
                            history.add(nextWord)
                            tvGameInfo.text = "$query > $nextWord"
                            addWordToHistory(nextWord, nextWordDefinition)
                        }
                    }
                }
            }
        }
    }

    private fun updateScore() {
        score += 1
        runOnUiThread {
            tvScore.text = "점수: $score"
        }
    }

    private fun saveScore(nickname: String, score: Int) {
        try {
            if(score > 0) {
                val file = File(filesDir, "score.txt")
                val fileWriter = FileWriter(file, true)
                fileWriter.write("$nickname:$score\n")
                fileWriter.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun resetGame() {
        history.clear()
        score = 0
        runOnUiThread {
            tvScore.text = "점수: $score"
            tvDefinition.text = "단어의 뜻이 여기에 표시됩니다."
            etUserInput.text.clear()
            llHistory.removeAllViews()
            btnQuit.isEnabled = false
            btnSubmit.isEnabled = false
            stopTimer()
        }
    }

    private fun restartGame() {
        history.clear()
        score = 0
        tvScore.text = "점수: $score"
        tvDefinition.text = "단어의 뜻이 여기에 표시됩니다."
        etUserInput.text.clear()
        llHistory.removeAllViews()
        tvGameInfo.text = "게임을 다시 시작합니다."
        btnQuit.isEnabled = true
        btnSubmit.isEnabled = true
        btnRank.isEnabled = false
        startTimer()
    }

    private fun quitGame() {
        val nickname = findViewById<TextView>(R.id.tvNickname).text.toString().replace("닉네임: ", "")
        if (score > 0) {
            saveScore(nickname, score)
        }
        resetGame()
        tvGameInfo.text = "컴퓨터에 승리! 게임을 다시 시작합니다."
        btnRank.isEnabled = true
    }

    private suspend fun checkExists(query: String): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val url = "https://krdict.korean.go.kr/api/search?key=$apiKey&part=word&sort=popular&num=100&pos=1&q=$query"
        val response = URL(url).readText()
        val words = midReturnAll(response, "<item>", "</item>")
        for (word in words) {
            if (midReturn(word, "<word>", "</word>") == query) {
                val definition = midReturn(word, "<definition>", "</definition>")
                val pos = midReturn(word, "<pos>", "</pos>")
                return@withContext definition to (pos == "명사")
            }
        }
        return@withContext "" to false
    }

    private suspend fun findWord(query: String): String = withContext(Dispatchers.IO) {
        var words = searchWords(query.last().toString())
        if (words.isEmpty()) {
            val lastChar = query.last()
            val modifiedChar = when (lastChar) {
                'ㄹ' -> 'ㄴ'
                'ㄴ' -> 'ㅇ'
                else -> null
            }
            if (modifiedChar != null) {
                words = searchWords(modifiedChar.toString())
            }
        }
        return@withContext if (words.isNotEmpty()) words.random() else ""
    }

    private suspend fun searchWords(query: String): List<String> {
        val url = "https://krdict.korean.go.kr/api/search?key=$apiKey&part=word&pos=1&q=$query*"
        val response = URL(url).readText()
        val words = midReturnAll(response, "<item>", "</item>")
        return words.filter { word ->
            val wordText = midReturn(word, "<word>", "</word>")
            wordText.length > 1 && midReturn(word, "<pos>", "</pos>") == "명사" && wordText !in history
        }.map { midReturn(it, "<word>", "</word>") }
    }


    private fun midReturn(value: String, start: String, end: String): String {
        return value.substringAfter(start, "").substringBefore(end, "")
    }

    private fun midReturnAll(value: String, start: String, end: String): List<String> {
        return value.split(start).drop(1).map { it.substringBefore(end) }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWordToHistory(word: String, definition: String) {
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.item_word_definition, llHistory, false)

        val tvWord = itemView.findViewById<TextView>(R.id.tvWord)
        val tvDefinition = itemView.findViewById<TextView>(R.id.tvDefinition)

        tvWord.text = word
        tvDefinition.text = definition

        llHistory.addView(itemView, 0) // Add at the top
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    tvTimer.text = "남은 시간: ${millisUntilFinished / 1000}초"
                }
            }

            override fun onFinish() {
                runOnUiThread {
                    showToast("시간 초과! 게임 종료")
                    quitGame()
                }
            }
        }.start()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        startTimer()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
    }
}
