package com.shahed.braumono

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

class BrauKeyboardView(context: Context, private val onKey: (String) -> Unit) : View(context) {
    private val bg = Color.rgb(36, 38, 38)
    private val green = Color.rgb(75, 255, 97)
    private val yellow = Color.rgb(244, 211, 94)
    private val white = Color.WHITE
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("monospace", Typeface.NORMAL)
    }
    private var shifted = false
    private var symbols = false
    private data class Key(val label: String, val x: Float, val y: Float, val w: Float, val h: Float, val color: Int)
    private val keys = mutableListOf<Key>()

    fun toggleShift() { shifted = !shifted; invalidate() }
    fun toggleSymbols() { symbols = !symbols; invalidate() }
    fun outputFor(label: String): String = if (label.length == 1 && label[0].isLetter()) {
        if (shifted) label.uppercase() else label.lowercase()
    } else label

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, (resources.displayMetrics.density * 270).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(bg)
        keys.clear()
        val d = resources.displayMetrics.density
        val width = width.toFloat()
        val top = 10f*d
        val rowH = 49f*d
        val gap = 2.5f*d
        val side = 5f*d

        fun addRow(labels: List<String>, y: Float, colorFor: (Int)->Int, inset: Float = 0f) {
            val available = width - 2*side - inset*2
            val keyW = (available - gap*(labels.size-1))/labels.size
            labels.forEachIndexed { i, label ->
                val x = side + inset + i*(keyW+gap)
                keys.add(Key(label, x, y, keyW, rowH, colorFor(i)))
            }
        }

        val rows = if (!symbols) listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("⇧","z","x","c","v","b","n","m","⌫")
        ) else listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","$","%","&","-","+","(",")"),
            listOf("ABC","/",";",":","!","?","'","\"","⌫")
        )

        addRow(rows[0], top, { green })
        addRow(rows[1], top+rowH+gap, { if (it%2==0) yellow else white }, 9f*d)
        addRow(rows[2], top+2*(rowH+gap), { green })

        val bottomY = top+3*(rowH+gap)+2f*d
        val bottomH = 43f*d
        val controls = listOf("123", ",", "SPACE", ".", "↵")
        val weights = listOf(1.1f, .65f, 4.3f, .65f, 1.1f)
        val totalWeight = weights.sum()
        val totalGap = gap*(controls.size-1)
        var x = side
        controls.forEachIndexed { i, label ->
            val kw = (width-2*side-totalGap)*weights[i]/totalWeight
            keys.add(Key(label, x, bottomY, kw, bottomH, when(label) {
                "SPACE", "," , "." -> white
                else -> green
            }))
            x += kw+gap
        }

        keys.forEach { k ->
            paint.color = k.color
            paint.textSize = when(k.label) { "SPACE" -> 12f*d; "123","ABC","⇧","⌫","↵" -> 17f*d; else -> 21f*d }
            val text = when {
                k.label == "SPACE" -> "English"
                k.label.length == 1 && k.label[0].isLetter() && shifted -> k.label.uppercase()
                else -> k.label
            }
            val baseline = k.y + k.h/2f - (paint.ascent()+paint.descent())/2f
            canvas.drawText(text, k.x+k.w/2f, baseline, paint)
        }
        // Thin neon-green top and bottom rules; no key boxes or borders.
        paint.color = green
        paint.strokeWidth = 2f*d
        canvas.drawLine(0f, 0f, width, 0f, paint)
        canvas.drawLine(0f, height-2f*d, width, height-2f*d, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x; val y = event.y
            val key = keys.lastOrNull { x >= it.x && x <= it.x+it.w && y >= it.y && y <= it.y+it.h }
            key?.let { onKey(it.label) }
            performClick()
            return true
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
}
