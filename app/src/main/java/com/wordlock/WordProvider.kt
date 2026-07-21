package com.wordlock

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar

data class Word(
    val word: String,
    val pronunciation: String,
    val category: String,
    val meaning: String,
    val meaningNP: String,
    val example: String,
    val synonyms: List<String>
)

object WordProvider {

    private var words: List<Word> = emptyList()

    private fun loadWords(context: Context): List<Word> {
        if (words.isNotEmpty()) return words

        val stream = context.resources.openRawResource(R.raw.words)
        val reader = BufferedReader(InputStreamReader(stream))
        val json = reader.readText()
        reader.close()

        val arr = JSONArray(json)
        words = (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val syns = obj.getJSONArray("synonyms")
            Word(
                word = obj.getString("word"),
                pronunciation = obj.getString("pronunciation"),
                category = obj.getString("category"),
                meaning = obj.getString("meaning"),
                meaningNP = obj.optString("meaningNP", ""),
                example = obj.getString("example"),
                synonyms = (0 until syns.length()).map { syns.getString(it) }
            )
        }
        return words
    }

    fun getDailyWord(context: Context): Word {
        val all = loadWords(context)
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return all[dayOfYear % all.size]
    }

    fun getRandomWord(context: Context): Word {
        val all = loadWords(context)
        return all[(Math.random() * all.size).toInt()]
    }
}
