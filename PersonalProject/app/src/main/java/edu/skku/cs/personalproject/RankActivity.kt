package edu.skku.cs.personalproject

import android.os.Bundle
import android.widget.BaseAdapter
import android.widget.ListView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader

class RankActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private val scoresList = mutableListOf<Pair<String, Int>>() // Pair of nickname and score
    private val fileName = "score.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank)

        listView = findViewById(R.id.listView)

        val nickname = intent.getStringExtra("nickname")
        val score = intent.getIntExtra("score", -1)
        if (score > 0 && nickname != null) {
            saveScore(nickname, score)
        }

        loadScores()
    }

    private fun saveScore(nickname: String, score: Int) {
        try {
            val file = File(filesDir, fileName)
            val fileWriter = FileWriter(file, true)
            fileWriter.write("$nickname:$score\n")
            fileWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadScores() {
        try {
            val file = File(filesDir, fileName)
            if (file.exists()) {
                val fileReader = InputStreamReader(openFileInput(fileName))
                val bufferedReader = BufferedReader(fileReader)
                var line: String?
                scoresList.clear()
                while (bufferedReader.readLine().also { line = it } != null) {
                    val parts = line!!.split(":")
                    if (parts.size == 2) {
                        val nickname = parts[0]
                        val score = parts[1].toIntOrNull()
                        if (score != null) {
                            scoresList.add(nickname to score)
                        }
                    }
                }
                bufferedReader.close()
                scoresList.sortByDescending { it.second }
                updateScoresAdapter()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun updateScoresAdapter() {
        val adapter = object : BaseAdapter() {
            override fun getCount(): Int = scoresList.size

            override fun getItem(position: Int): Any = scoresList[position]

            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View = convertView ?: LayoutInflater.from(this@RankActivity).inflate(R.layout.list_item_score, parent, false)
                val nicknameTextView = view.findViewById<TextView>(R.id.tvNickname)
                val scoreTextView = view.findViewById<TextView>(R.id.tvScore)

                val (nickname, score) = getItem(position) as Pair<String, Int>
                nicknameTextView.text = nickname
                scoreTextView.text = score.toString()

                return view
            }
        }
        listView.adapter = adapter
    }
}
