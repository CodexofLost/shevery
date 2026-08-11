package com.hamondev.shevery.tasker

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.json.JSONObject
import rikka.shizuku.Shizuku

class PluginReceiver : BroadcastReceiver() {

    private val controlComponent = ComponentName(
        PluginContract.MANAGER_PACKAGE,
        PluginContract.MANAGER_CONTROL_RECEIVER
    )

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            PluginContract.ACTION_FIRE_SETTING -> handleFire(context, intent)
            PluginContract.ACTION_QUERY_CONDITION -> handleQuery(intent)
        }
    }

    private fun handleFire(context: Context, intent: Intent) {
        if (!isExplicit(intent)) return
        val command = parseCommand(intent.getBundleExtra(PluginContract.EXTRA_BUNDLE)) ?: return
        when (command) {
            Command.START -> sendControl(context, PluginContract.ACTION_START_SERVER)
            Command.STOP -> sendControl(context, PluginContract.ACTION_STOP_SERVER)
            Command.RESTART -> restartServer(context)
            Command.TOGGLE -> if (Shizuku.pingBinder()) {
                sendControl(context, PluginContract.ACTION_STOP_SERVER)
            } else {
                sendControl(context, PluginContract.ACTION_START_SERVER)
            }
        }
        resultCode = Activity.RESULT_OK
    }

    private fun handleQuery(intent: Intent) {
        if (!isExplicit(intent)) return
        val bundle = intent.getBundleExtra(PluginContract.EXTRA_BUNDLE) ?: return
        val json = bundle.getString(PluginContract.EXTRA_STRING_JSON) ?: return
        val condition = runCatching {
            JSONObject(json).optString(PluginContract.KEY_CONDITION)
        }.getOrNull()
        if (condition != PluginContract.VALUE_CONDITION_RUNNING) return
        resultCode = if (Shizuku.pingBinder()) {
            PluginContract.RESULT_CONDITION_SATISFIED
        } else {
            PluginContract.RESULT_CONDITION_UNSATISFIED
        }
    }

    private fun restartServer(context: Context) {
        sendControl(context, PluginContract.ACTION_STOP_SERVER)
        val pendingResult = goAsync()
        Thread {
            try {
                val deadline = System.currentTimeMillis() + RESTART_WAIT_MS
                while (System.currentTimeMillis() < deadline && Shizuku.pingBinder()) {
                    Thread.sleep(RESTART_POLL_MS)
                }
                sendControl(context, PluginContract.ACTION_START_SERVER)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    private fun sendControl(context: Context, action: String) {
        val intent = Intent(action).apply {
            setPackage(PluginContract.MANAGER_PACKAGE)
            component = controlComponent
        }
        context.sendBroadcast(intent)
    }

    private fun isExplicit(intent: Intent): Boolean {
        val component = intent.component ?: return false
        return component.packageName == PluginContract.MANAGER_PACKAGE &&
            component.className == PluginReceiver::class.java.name
    }

    private fun parseCommand(bundle: Bundle?): Command? {
        val json = bundle?.getString(PluginContract.EXTRA_STRING_JSON) ?: return null
        return runCatching {
            Command.from(JSONObject(json).optString(PluginContract.KEY_COMMAND))
        }.getOrNull()
    }

    companion object {
        private const val RESTART_WAIT_MS = 10_000L
        private const val RESTART_POLL_MS = 250L
    }
}
