package edu.skku.cs.personalproject

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class WordSearchActivity : AppCompatActivity() {
    private lateinit var etInput: EditText
    private lateinit var btnSearch: Button
    private lateinit var llResults: LinearLayout
    private lateinit var progressBar: ProgressBar
    private val apiKey = "A4429A1B2FDEFC1ED54A3DE39BE2C8F3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_search)

        etInput = findViewById(R.id.etInput)
        btnSearch = findViewById(R.id.btnSearch)
        llResults = findViewById(R.id.llResults)
        progressBar = findViewById(R.id.progressBar)

        etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                etInput.setTextColor(Color.BLACK)
            }
        })

        btnSearch.setOnClickListener {
            val userInput = etInput.text.toString()
            if (userInput.length != 1 || !isValidKoreanChar(userInput[0])) {
                Toast.makeText(this, "유효한 한 글자를 입력하세요.", Toast.LENGTH_SHORT).show()
                etInput.setTextColor(Color.RED)
            } else {
                searchWords(userInput)
            }
        }
    }

    private fun isValidKoreanChar(char: Char): Boolean {
        return char in '가'..'힣'
    }

    private fun searchWords(query: String) {
        progressBar.visibility = ProgressBar.VISIBLE
        llResults.removeAllViews()

        CoroutineScope(Dispatchers.IO).launch {
            val words = fetchWords(query)
            withContext(Dispatchers.Main) {
                progressBar.visibility = ProgressBar.GONE
                if (words.isEmpty()) {
                    Toast.makeText(this@WordSearchActivity, "해당 단어로 시작하는 명사가 없습니다.", Toast.LENGTH_SHORT).show()
                    etInput.setTextColor(Color.RED)
                } else {
                    displayResults(words)
                }
            }
        }
    }

    private suspend fun fetchWords(query: String): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val url = "https://krdict.korean.go.kr/api/search?key=$apiKey&part=word&sort=popular&num=100&pos=1&q=$query*"
        val response = URL(url).readText()
        val words = midReturnAll(response, "<item>", "</item>")
        return@withContext words.mapNotNull { word ->
            val wordText = midReturn(word, "<word>", "</word>")
            val definition = midReturn(word, "<definition>", "</definition>")
            val pos = midReturn(word, "<pos>", "</pos>")
            if (pos == "명사" && wordText.length > 1) wordText to definition else null
        }
    }

    private fun midReturn(value: String, start: String, end: String): String {
        val startIndex = value.indexOf(start)
        val endIndex = value.indexOf(end, startIndex + start.length)
        return if (startIndex != -1 && endIndex != -1) {
            value.substring(startIndex + start.length, endIndex)
        } else {
            ""
        }
    }

    private fun midReturnAll(value: String, start: String, end: String): List<String> {
        return value.split(start).drop(1).map { it.substringBefore(end) }
    }

    private fun displayResults(words: List<Pair<String, String>>) {
        llResults.removeAllViews()
        words.forEach { (word, definition) ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_word_definition, llResults, false)
            view.findViewById<TextView>(R.id.tvWord).text = word
            view.findViewById<TextView>(R.id.tvDefinition).text = definition
            llResults.addView(view)
        }
    }
}
