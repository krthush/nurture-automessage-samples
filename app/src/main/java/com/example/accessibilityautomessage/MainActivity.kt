package com.example.accessibilityautomessage

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var whatsAppText = "";

    override fun onResume() {
        super.onResume()
        checkButtonStates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val packageContext: Context = this;
        val whatsappMessageButton: Button = findViewById(R.id.whatsapp_message_button)
        val permissionButton: Button = findViewById(R.id.permission_button)
        val whatsappAutoMessageButton: Button = findViewById(R.id.whatsapp_auto_message_button)
        val facebookAutoMessageButton: Button = findViewById(R.id.facebook_auto_message_button)

        checkButtonStates()

        whatsappMessageButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Code here executes on main thread after user presses button
                whatsAppText = applicationContext.getString(R.string.sample_message)
                whatsappIntent()
            }
        })

        permissionButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Code here executes on main thread after user presses button
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                packageContext.startActivity(intent)
            }
        })

        whatsappAutoMessageButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Code here executes on main thread after user presses button
                whatsAppText = "message"
                setClipboardMessage()
                whatsappIntent()
            }
        })

        facebookAutoMessageButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Code here executes on main thread after user presses button
                setClipboardMessage()
                facebookIntent()
            }
        })

    }

    protected  fun setClipboardMessage() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // Creates a new text clip to put on the clipboard
        val clip: ClipData = ClipData.newPlainText("simple text", applicationContext.getString(R.string.sample_message) + applicationContext.getString(R.string.automessage_suffix))
        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clip)
    }

    protected fun facebookIntent() {
        val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse(applicationContext.getString(R.string.fb_profile_uri)))
        startActivity(sendIntent)
    }

    protected fun whatsappIntent() {
        val smsNumber = applicationContext.getString(R.string.whatsapp_phone_number) // E164 format without '+' sign
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, whatsAppText)
        sendIntent.putExtra("jid", "$smsNumber@s.whatsapp.net") //phone number without "+" prefix
        sendIntent.setPackage("com.whatsapp")
        startActivity(sendIntent)
    }

    private fun checkButtonStates() {
        val permissionButton: Button = findViewById(R.id.permission_button)
        val whatsappAutoMessageButton: Button = findViewById(R.id.whatsapp_auto_message_button)
        val facebookAutoMessageButton: Button = findViewById(R.id.facebook_auto_message_button)
        if (isAccessibilityOn(this, MyAccessibilityService::class.java)) {
            permissionButton.isEnabled = false
            permissionButton.text = "Permission granted";
            whatsappAutoMessageButton.visibility = VISIBLE;
            facebookAutoMessageButton.visibility = VISIBLE;
        } else {
            permissionButton.isEnabled = true
            whatsappAutoMessageButton.visibility = INVISIBLE;
            facebookAutoMessageButton.visibility = INVISIBLE;
        }
    }

    private fun isAccessibilityOn(
        context: Context,
        clazz: Class<out AccessibilityService?>
    ): Boolean {
        var accessibilityEnabled = 0
        val service = context.packageName + "/" + clazz.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (ignored: SettingNotFoundException) {
        }
        val colonSplitter = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                colonSplitter.setString(settingValue)
                while (colonSplitter.hasNext()) {
                    val accessibilityService = colonSplitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

}
