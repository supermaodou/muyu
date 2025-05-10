package com.example.muyu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MuyuWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        initializeSoundPool(context)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            "com.example.muyu.KNOCK_MUYU" -> {
                // 防抖
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastKnockTime < minKnockInterval) {
                    return
                }
                lastKnockTime = currentTime

                // 播放音效
                initializeSoundPool(context)
                soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)

                // 更新敲击次数
                val knockCount = PreferenceManager.getKnockCount(context) + 1
                PreferenceManager.setKnockCount(context, knockCount)

                // 更新所有小部件
                updateAllWidgets(context)

                // 通知主页面更新
                val updateIntent = Intent("com.example.muyu.UPDATE_MAIN")
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
            }
            "com.example.muyu.UPDATE_WIDGET" -> {
                // 主页面更新时刷新小部件
                updateAllWidgets(context)
            }
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        releaseSoundPool()
    }

    companion object {
        private var soundPool: SoundPool? = null
        private var soundId = 0
        private var lastKnockTime = 0L
        private const val minKnockInterval = 100L

        fun initializeSoundPool(context: Context) {
            if (soundPool == null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                soundPool = SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build()
                soundId = soundPool!!.load(context, R.raw.muyu_sound, 1)
            }
        }

        fun releaseSoundPool() {
            soundPool?.release()
            soundPool = null
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // 读取敲击次数
            val knockCount = PreferenceManager.getKnockCount(context)
            views.setTextViewText(R.id.widgetKnockCount, "功德: $knockCount")

            // 设置点击事件
            val intent = Intent(context, MuyuWidgetProvider::class.java).apply {
                action = "com.example.muyu.KNOCK_MUYU"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetMuyuImage, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, MuyuWidgetProvider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}