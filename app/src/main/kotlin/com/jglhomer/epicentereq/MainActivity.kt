package com.jglhomer.epicentereq

import android.content.Intent
import android.graphics.Typeface
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ryanheise.just_audio.EpicenterDsp
import com.ryanheise.just_audio.EpicenterEngine
import com.ryanheise.just_audio.EpicenterParams
import com.ryanheise.just_audio.EpicenterProcessorController
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    private lateinit var playersText: TextView
    private lateinit var sessionsText: TextView
    private lateinit var backendText: TextView
    private lateinit var testText: TextView
    private var selectedEngine = EpicenterEngine.HYBRID
    private var params = EpicenterParams()
    private var enabled = false

    private val sessionListener: () -> Unit = { runOnUiThread { renderTracking() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeEffectPanelIntent(intent)
        setContentView(buildUi())
        SessionStore.addListener(sessionListener)
        renderTracking()
        renderBackend()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeEffectPanelIntent(intent)
        renderTracking()
    }

    override fun onDestroy() {
        SessionStore.removeListener(sessionListener)
        super.onDestroy()
    }

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(32))
        }
        val scroll = ScrollView(this).apply { addView(root) }

        root.addView(TextView(this).apply {
            text = "JGL EPICENTER EQ"
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "Motor DSP copiado 1:1 de jgl-homer/player. Tracking de reproductores y sesiones estilo AudioEffect."
            textSize = 14f
            setPadding(0, dp(6), 0, dp(18))
        })

        backendText = sectionText(root, "Backend")
        val probeButton = Button(this).apply {
            text = "Volver a detectar backend de sistema"
            setOnClickListener { renderBackend() }
        }
        root.addView(probeButton)

        playersText = sectionText(root, "Reproductores activos")
        sessionsText = sectionText(root, "Sesiones AudioEffect")

        root.addView(Button(this).apply {
            text = "Dar acceso a notificaciones / MediaSession"
            setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        })

        addDivider(root)
        root.addView(TextView(this).apply {
            text = "EPICENTER"
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
        })

        val enabledSwitch = Switch(this).apply {
            text = "Epicenter ON"
            isChecked = enabled
            setOnCheckedChangeListener { _, checked ->
                enabled = checked
                EpicenterProcessorController.setEpicenterEnabled(checked)
            }
        }
        root.addView(enabledSwitch)

        val engineSpinner = Spinner(this)
        val engines = EpicenterEngine.entries.map { it.name }
        engineSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, engines)
        engineSpinner.setSelection(engines.indexOf(EpicenterEngine.HYBRID.name))
        engineSpinner.onItemSelectedListener = SimpleItemSelectedListener { position ->
            selectedEngine = EpicenterEngine.entries[position]
            EpicenterProcessorController.setEngine(selectedEngine)
        }
        root.addView(label("Engine"))
        root.addView(engineSpinner)

        addSlider(root, "Sweep", 27, 63, params.sweepFreq.toInt(), "Hz") {
            params = params.copy(sweepFreq = it.toFloat())
            EpicenterProcessorController.setSweepFreq(it.toFloat())
        }
        addSlider(root, "Width", 0, 100, params.width.toInt(), "%") {
            params = params.copy(width = it.toFloat())
            EpicenterProcessorController.setWidth(it.toFloat())
        }
        addSlider(root, "Intensity", 0, 100, params.intensity.toInt(), "%") {
            params = params.copy(intensity = it.toFloat())
            EpicenterProcessorController.setIntensity(it.toFloat())
        }
        addSlider(root, "Volume", 0, 100, params.volume.toInt(), "%") {
            params = params.copy(volume = it.toFloat())
            EpicenterProcessorController.setVolume(it.toFloat())
        }

        root.addView(Switch(this).apply {
            text = "Peak Protection"
            isChecked = params.peakProtectionEnabled
            setOnCheckedChangeListener { _, checked ->
                params = params.copy(peakProtectionEnabled = checked)
                EpicenterProcessorController.setPeakProtectionEnabled(checked)
            }
        })

        addDivider(root)
        root.addView(Button(this).apply {
            text = "Probar motor DSP local"
            setOnClickListener { runSelfTest() }
        })
        testText = TextView(this).apply {
            text = "Self-test: sin ejecutar"
            setPadding(0, dp(8), 0, 0)
        }
        root.addView(testText)

        return scroll
    }

    private fun renderTracking() {
        val active = SessionStore.activePackages()
        playersText.text = buildString {
            append("Reproductores activos\n")
            if (active.isEmpty()) append("• Ninguno detectado por MediaSession")
            else active.forEach { pkg -> append("• ${appLabel(pkg)}  ($pkg)\n") }
        }.trimEnd()

        val sessions = SessionStore.sessions()
        sessionsText.text = buildString {
            append("Sesiones AudioEffect\n")
            if (sessions.isEmpty()) append("• Ningún reproductor anunció OPEN_AUDIO_EFFECT_CONTROL_SESSION")
            else sessions.forEach { s ->
                val pkg = s.packageName.ifBlank { "paquete desconocido" }
                append("• ${appLabel(pkg)} · session ${s.sessionId} · content ${s.contentType}\n")
            }
        }.trimEnd()
    }

    private fun renderBackend() {
        val result = EffectProbe.scan()
        backendText.text = if (result.found) {
            "Backend\n• Se detectó un efecto de sistema JGL/Epicenter:\n${result.descriptions.joinToString("\n") { "  $it" }}"
        } else {
            "Backend\n• Core DSP: cargado en la APK.\n• Efecto insertable de sistema JGL: NO detectado.\n• En este estado la app reconoce sesiones, pero Android no entrega el PCM externo al Kotlin DSP."
        }
    }

    private fun runSelfTest() {
        val rate = 48_000
        val seconds = 0.35
        val frames = (rate * seconds).toInt()
        val input = FloatArray(frames * 2)
        val output = FloatArray(input.size)
        for (i in 0 until frames) {
            val t = i.toDouble() / rate
            val x = (0.16 * sin(2 * PI * 72 * t) +
                0.12 * sin(2 * PI * 108 * t) +
                0.08 * sin(2 * PI * 144 * t)).toFloat()
            input[i * 2] = x
            input[i * 2 + 1] = x
        }
        val dsp = EpicenterDsp(rate, selectedEngine, debugEnabled = true)
        dsp.prepare(2, params)
        dsp.processInterleaved(input, output, 2, params, enabled = true)
        var inputPeak = 0f
        var outputPeak = 0f
        var deltaPeak = 0f
        for (i in input.indices) {
            inputPeak = max(inputPeak, abs(input[i]))
            outputPeak = max(outputPeak, abs(output[i]))
            deltaPeak = max(deltaPeak, abs(output[i] - input[i]))
        }
        testText.text = "Self-test ${selectedEngine.name}: input peak %.4f · output peak %.4f · delta %.4f · f0 %.2f Hz · confidence %.3f"
            .format(inputPeak, outputPeak, deltaPeak, dsp.debugState.detectedF0, dsp.debugState.confidence)
    }

    private fun consumeEffectPanelIntent(intent: Intent?) {
        if (intent?.action != AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL) return
        val session = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
        if (session < 0) return
        SessionStore.open(
            PlayerSession(
                sessionId = session,
                packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME).orEmpty(),
                contentType = intent.getIntExtra(AudioEffect.EXTRA_CONTENT_TYPE, 0),
            )
        )
    }

    private fun sectionText(parent: LinearLayout, title: String): TextView = TextView(this).also {
        it.text = "$title\n• Cargando…"
        it.textSize = 15f
        it.setPadding(0, dp(14), 0, dp(10))
        parent.addView(it)
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setPadding(0, dp(12), 0, dp(4))
    }

    private fun addSlider(
        parent: LinearLayout,
        name: String,
        min: Int,
        max: Int,
        initial: Int,
        suffix: String,
        onChange: (Int) -> Unit,
    ) {
        val value = TextView(this).apply { textSize = 15f }
        val seek = SeekBar(this).apply {
            this.max = max - min
            progress = initial - min
        }
        fun render(v: Int) { value.text = "$name: $v $suffix" }
        render(initial)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actual = progress + min
                render(actual)
                onChange(actual)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        parent.addView(value)
        parent.addView(seek)
    }

    private fun addDivider(parent: LinearLayout) {
        parent.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)).apply {
                topMargin = dp(18); bottomMargin = dp(18)
            }
            setBackgroundColor(0x44FFFFFF)
        })
    }

    private fun appLabel(packageName: String): String {
        if (packageName.isBlank()) return "Desconocido"
        return runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
