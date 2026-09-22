package com.harasyounis

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.Typeface
import android.net.VpnService
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private lateinit var protectionSwitch: Switch
    private lateinit var status: TextView

    private val blue = Color.rgb(25, 87, 145)
    private val bg = Color.rgb(246, 248, 251)
    private val card = Color.WHITE
    private val muted = Color.rgb(100, 110, 120)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("rules", MODE_PRIVATE)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 30, 28, 22)
            setBackgroundColor(blue)
        }

        val title = TextView(this).apply {
            text = "🛡️ حارس يونس"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.RIGHT
        }
        header.addView(title)

        val subtitle = TextView(this).apply {
            text = "تحكم في اتصال تطبيقاتك بالإنترنت"
            textSize = 16f
            setTextColor(Color.WHITE)
            alpha = 0.9f
            gravity = Gravity.RIGHT
            setPadding(0, 6, 0, 0)
        }
        header.addView(subtitle)

        root.addView(header)

        val protectionCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(22, 18, 22, 18)
            setBackgroundColor(card)
        }

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        status = TextView(this).apply {
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.RIGHT
        }
        labels.addView(status)

        val hint = TextView(this).apply {
            text = "تطبيق القواعد على الشبكة الحالية"
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.RIGHT
        }
        labels.addView(hint)

        protectionSwitch = Switch(this).apply {
            isChecked = prefs.getBoolean("protection", false)
            setOnCheckedChangeListener { _, checked ->
                if (checked) enableProtection() else disableProtection()
            }
        }

        protectionCard.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        protectionCard.addView(protectionSwitch)
        root.addView(protectionCard)

        val section = TextView(this).apply {
            text = "التطبيقات"
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.DKGRAY)
            setPadding(24, 18, 24, 8)
            gravity = Gravity.RIGHT
        }
        root.addView(section)

        search = EditText(this).apply {
        this.hint = "🔎  ابحث عن تطبيق"
        setSingleLine(true)
            setPadding(20, 8, 20, 8)
            setBackgroundColor(Color.WHITE)
        }
        val searchParams = LinearLayout.LayoutParams(-1, 52)
        searchParams.setMargins(18, 0, 18, 8)
        root.addView(search, searchParams)

        val legend = TextView(this).apply {
            text = "📶 Wi‑Fi        📱 بيانات الجوال"
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.RIGHT
            setPadding(24, 0, 24, 8)
        }
        root.addView(legend)

        val scroll = ScrollView(this)
        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 0, 18, 24)
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                renderApps(s?.toString() ?: "")
            }
            override fun afterTextChanged(e: android.text.Editable?) {}
        })

        
        val settingsButton = Button(this).apply {
            text = "⚙️ الإعدادات"
            textSize = 15f
            setOnClickListener { showSettingsScreen() }
        }
        root.addView(settingsButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(16, 8, 16, 8) })

setContentView(root)
        updateStatus()
        renderApps("")
    }

    private fun enableProtection() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, 100)
        } else {
            prefs.edit().putBoolean("protection", true).apply()
            startService(Intent(this, YounisVpnService::class.java))
            updateStatus()
        }
    }

    private fun disableProtection() {
        prefs.edit().putBoolean("protection", false).apply()
        stopService(Intent(this, YounisVpnService::class.java))
        updateStatus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                prefs.edit().putBoolean("protection", true).apply()
                startService(Intent(this, YounisVpnService::class.java))
            } else {
                protectionSwitch.isChecked = false
            }
            updateStatus()
        }
    }

    private fun updateStatus() {
        status.text = if (prefs.getBoolean("protection", false))
            "🟢 الحماية مفعّلة"
        else
            "⚪ الحماية متوقفة"
    }

    private fun renderApps(filter: String) {
        list.removeAllViews()
        val pm = packageManager

        val apps = pm.getInstalledApplications(0)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase(Locale.getDefault()) }

        for (app in apps) {
            val name = pm.getApplicationLabel(app).toString()
            if (filter.isNotBlank() && !name.contains(filter, true)) continue

            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(18, 16, 18, 14)
                setBackgroundColor(card)
                elevation = 2f
                layoutDirection = View.LAYOUT_DIRECTION_RTL
            }

            val top = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = ImageView(this).apply {
                setImageDrawable(pm.getApplicationIcon(app))
            }
            top.addView(icon, LinearLayout.LayoutParams(52, 52))

            val nameView = TextView(this).apply {
                text = name
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.RIGHT
                setPadding(12, 0, 12, 0)
            }
            top.addView(nameView, LinearLayout.LayoutParams(0, -2, 1f))
            item.addView(top)

            val wifi = Switch(this).apply {
                text = "📶  حجب Wi‑Fi"
                textSize = 15f
                isChecked = prefs.getBoolean("${app.packageName}_wifi", false)
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean("${app.packageName}_wifi", checked).apply()
                    restartProtectionIfNeeded()
                }
            }

            val mobile = Switch(this).apply {
                text = "📱  حجب بيانات الجوال"
                textSize = 15f
                isChecked = prefs.getBoolean("${app.packageName}_mobile", false)
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean("${app.packageName}_mobile", checked).apply()
                    restartProtectionIfNeeded()
                }
            }

            item.addView(wifi)
            item.addView(mobile)

            val params = LinearLayout.LayoutParams(-1, -2)
            params.setMargins(0, 0, 0, 12)
            list.addView(item, params)
        }
    }

    private fun restartProtectionIfNeeded() {
        if (prefs.getBoolean("protection", false)) {
            startService(Intent(this, YounisVpnService::class.java))
        }
    }
    private fun showSettingsScreen() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }

        val title = TextView(this).apply {
            text = "⚙️ إعدادات حارس يونس"
            textSize = 24f
            setPadding(0, 0, 0, 20)
        }
        layout.addView(title)

        fun addSwitch(label: String, key: String, default: Boolean): Switch {
            val sw = Switch(this).apply {
                text = label
                textSize = 17f
                isChecked = prefs.getBoolean(key, default)
                setPadding(0, 14, 0, 14)
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean(key, checked).apply()
                }
            }
            layout.addView(sw, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
            return sw
        }

        addSwitch("🌙 الوضع الداكن", "dark_mode", false)
        addSwitch("🛡️ تشغيل الحماية تلقائيًا", "auto_protect", true)
        addSwitch("🔄 إعادة الحماية عند تغيير الشبكة", "network_reconnect", true)
        addSwitch("🔔 إشعار حالة الحماية", "status_notification", true)

        val divider1 = View(this).apply { setBackgroundColor(0xFFE0E0E0.toInt()) }
        layout.addView(divider1, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        ).apply { setMargins(0, 10, 0, 16) })

        val advancedTitle = TextView(this).apply {
            text = "الإعدادات المتقدمة"
            textSize = 19f
            setPadding(0, 0, 0, 8)
        }
        layout.addView(advancedTitle)

        val strictSwitch = addSwitch("🔒 وضع الحماية المشددة", "strict_mode", false)

        val dnsSwitch = addSwitch("🌐 منع DNS للتطبيقات المحجوبة", "block_dns", true)

        val note = TextView(this).apply {
            text = "ملاحظة: بعض خيارات الحماية تعتمد على إمكانيات Android وإصدار الجهاز، وقد تحتاج إعادة تشغيل الحماية لتطبيق التغيير."
            textSize = 13f
            setPadding(0, 14, 0, 14)
        }
        layout.addView(note)

        val reset = Button(this).apply {
            text = "↩️ إعادة الإعدادات الافتراضية"
            setOnClickListener {
                prefs.edit().clear().apply()
                Toast.makeText(this@MainActivity, "تمت إعادة الإعدادات", Toast.LENGTH_SHORT).show()
                showSettingsScreen()
            }
        }
        layout.addView(reset)

        val back = Button(this).apply {
            text = "← العودة"
            setOnClickListener { recreate() }
        }
        layout.addView(back)

        setContentView(ScrollView(this).apply { addView(layout) })
    }

}
