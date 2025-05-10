package com.example.muyu

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    private lateinit var soundPool: SoundPool
    private var soundId = 0
    private lateinit var knockCountText: TextView
    private lateinit var meritText: TextView
    private lateinit var eggText: TextView
    private lateinit var particleView: ParticleView
    private lateinit var muyuImage: ImageView
    private var knockCount = 0
    private var lastKnockTime = 0L
    private val minKnockInterval = 100L // 最小点击间隔，单位：毫秒
    private val eggTriggers = setOf(100, 500, 1000) // 彩蛋触发阈值

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 设置状态栏背景色和图标颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = 0xFF000000.toInt() // 黑色
            WindowCompat.getInsetsController(window, window.decorView).let { controller ->
                controller.isAppearanceLightStatusBars = true // 设置状态栏图标为浅色（白色）
            }
        }

        // 初始化 SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
        soundId = soundPool.load(this, R.raw.muyu_sound, 1)

        // 获取控件
        muyuImage = findViewById(R.id.muyuImage)
        knockCountText = findViewById(R.id.knockCount)
        meritText = findViewById(R.id.meritText)
        eggText = findViewById(R.id.eggText)
        particleView = findViewById(R.id.particleView)

        // 读取保存的敲击次数
        knockCount = PreferenceManager.getKnockCount(this)
        updateKnockCount()

        // 设置点击事件
        muyuImage.setOnClickListener {
            knockMuyu()
        }
    }

    private fun knockMuyu() {
        // 防抖：检查是否在最小间隔内
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastKnockTime < minKnockInterval) {
            return // 忽略过快的点击
        }
        lastKnockTime = currentTime

        // 播放音效
        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)

        // 木鱼缩放动画
        muyuImage.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(50)
            .withEndAction {
                muyuImage.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(50)
                    .start()
            }
            .start()

        // “功德+1”上浮动画
        meritText.visibility = View.VISIBLE
        meritText.alpha = 1.0f
        meritText.translationY = 0f
        meritText.animate()
            .translationY(-100f)
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                meritText.visibility = View.INVISIBLE
            }
            .start()

        // 更新敲击次数
        knockCount++
        updateKnockCount()
        PreferenceManager.setKnockCount(this, knockCount)

        // 检查彩蛋触发
        if (knockCount in eggTriggers) {
            triggerEgg()
        }

        // 通知小部件更新
        val intent = Intent(this, MuyuWidgetProvider::class.java).apply {
            action = "com.example.muyu.UPDATE_WIDGET"
        }
        sendBroadcast(intent)
    }

    private fun updateKnockCount() {
        knockCountText.text = "功德: $knockCount"
    }

    private fun triggerEgg() {
        // 显示“功德圆满！”动画
        eggText.visibility = View.VISIBLE
        eggText.alpha = 1.0f
        eggText.translationY = 0f
        eggText.animate()
            .translationY(-150f)
            .alpha(0f)
            .setDuration(1000)
            .withEndAction {
                eggText.visibility = View.INVISIBLE
            }
            .start()

        // 启动粒子动画
        particleView.startAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}