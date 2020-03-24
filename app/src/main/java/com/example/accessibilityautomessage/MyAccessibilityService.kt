package com.example.accessibilityautomessage

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.*
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat


class MyAccessibilityService : AccessibilityService() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if (rootInActiveWindow == null) {
            return
        }
        val rootInActiveWindow = AccessibilityNodeInfoCompat.wrap(rootInActiveWindow)

        if (rootInActiveWindow != null && rootInActiveWindow.packageName != null && rootInActiveWindow.packageName == "com.facebook.orca") {

            if (event.eventType == TYPE_VIEW_TEXT_CHANGED && event.source != null && event.source.className != null && event.source.className == "android.widget.EditText") {
                Log.i("nurturelogs", event.toString())
                Log.i("nurturelogs", rootInActiveWindow.getChild(0).getChild(1).toString())
                if (rootInActiveWindow.getChild(0).getChild(1).contentDescription.toString().contains("Stephen")) {
                    Log.i("nurturelogs", "Talking to Stephen")
                }
            }

            val clipBoardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val copiedString = clipBoardManager.primaryClip?.getItemAt(0)?.text?.toString()

            // So that service doesn't process any message, but the ones ending your apps suffix
            if (!copiedString.isNullOrBlank() && copiedString.endsWith(applicationContext.getString(R.string.automessage_suffix))) {

                Thread.sleep(500)

                // Messenger Message EditText
                val editTextNode: AccessibilityNodeInfoCompat? = findNodeInfosByClassName(rootInActiveWindow, "android.widget.EditText")
                if (editTextNode != null) {

                    // Set the EditText with the clipboard data
                    val arguments = Bundle()
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, copiedString.dropLast(applicationContext.getString(R.string.automessage_suffix).length))
                    editTextNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

                    // Clear the clipboard
                    clipBoardManager.clearPrimaryClip()

                }

            }

        } else if (rootInActiveWindow != null && rootInActiveWindow.packageName != null && rootInActiveWindow.packageName == "com.whatsapp") {

            if (event.eventType == TYPE_VIEW_TEXT_CHANGED && event.source != null && event.source.viewIdResourceName != null && event.source.viewIdResourceName == "com.whatsapp:id/entry") {
                Log.i("nurturelogs", event.toString())
                Log.i("nurturelogs", rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/conversation_contact_name").toString())
                if (rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/conversation_contact_name")[0].text == "Stephen") {
                    Log.i("nurturelogs", "Talking to Stephen")
                }
            }

            // Whatsapp Message EditText id
            val messageNodeList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry")
            if (messageNodeList != null && messageNodeList.isNotEmpty()) {

                val clipBoardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val copiedString = clipBoardManager.primaryClip?.getItemAt(0)?.text?.toString()

                // So that service doesn't process any message, but the ones ending your apps suffix
                if (!copiedString.isNullOrBlank() && copiedString.endsWith(applicationContext.getString(R.string.automessage_suffix))) {

                    // Set the EditText with the clipboard data
                    val arguments = Bundle()
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, copiedString.dropLast(applicationContext.getString(R.string.automessage_suffix).length))
                    messageNodeList[0].performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

                    // Clear the clipboard
                    clipBoardManager.clearPrimaryClip()

                    // Whatsapp send button id (for auto send)
                    val sendMessageNodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
                    if (sendMessageNodeInfoList != null && sendMessageNodeInfoList.isNotEmpty()) {
                        val sendMessageButton = sendMessageNodeInfoList[0]
                        if (sendMessageButton.isVisibleToUser) {
                            // Now fire a click on the send button (for auto send)
                            sendMessageButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    }

                }

            }

        }

        return

    }

    override fun onInterrupt() {}

    fun findNodeInfosByClassName(nodeInfo: AccessibilityNodeInfoCompat, className: String): AccessibilityNodeInfoCompat? {
        for (i in 0 until nodeInfo.childCount) {
            val node = nodeInfo.getChild(i)
            if (node != null) {
                if ((node.className != null) && (className == node.className)) {
                    return node
                } else if (node.childCount > 0) {
                    if (findNodeInfosByClassName(node, className) != null) {
                        return findNodeInfosByClassName(node, className)
                    }
                }
            }
        }
        return null
    }
}