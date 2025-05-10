package com.example.muyu

import android.content.Context
import androidx.preference.PreferenceManager

object PreferenceManager {
    private const val KEY_KNOCK_COUNT = "knock_count"

    fun getKnockCount(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(KEY_KNOCK_COUNT, 0)
    }

    fun setKnockCount(context: Context, count: Int): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.edit().putInt(KEY_KNOCK_COUNT, count).commit() // 使用 commit 确保同步写入
    }
}