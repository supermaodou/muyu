package com.example.muyu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class ParticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply { isAntiAlias = true }
    private var isAnimating = false

    data class Particle(
        var x: Float,
        var y: Float,
        var radius: Float,
        var color: Int,
        var vx: Float,
        var vy: Float,
        var alpha: Float
    )

    fun startAnimation() {
        if (!isAnimating) {
            isAnimating = true
            particles.clear()
            // 创建 50 个粒子
            repeat(50) {
                particles.add(
                    Particle(
                        x = width / 2f,
                        y = height / 2f,
                        radius = Random.nextFloat() * 5 + 5,
                        color = Color.rgb(
                            Random.nextInt(256),
                            Random.nextInt(256),
                            Random.nextInt(256)
                        ),
                        vx = (Random.nextFloat() - 0.5f) * 10,
                        vy = (Random.nextFloat() - 0.5f) * 10,
                        alpha = 1.0f
                    )
                )
            }
            visibility = VISIBLE
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isAnimating) return

        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.x += particle.vx
            particle.y += particle.vy
            particle.alpha -= 0.01f

            if (particle.alpha <= 0) {
                iterator.remove()
            } else {
                paint.color = particle.color
                paint.alpha = (particle.alpha * 255).toInt()
                canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
            }
        }

        if (particles.isEmpty()) {
            isAnimating = false
            visibility = INVISIBLE
        } else {
            invalidate()
        }
    }
}