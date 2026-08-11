package com.hamondev.shevery.tasker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import org.json.JSONObject

class EditActivity : Activity() {

    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isSetting = intent.action == PluginContract.ACTION_EDIT_SETTING
        val selection = restoreSelection()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.tasker_plugin_name)
            textSize = 20f
        })

        radioGroup = RadioGroup(this)
        if (isSetting) {
            Command.entries.forEach { command ->
                radioGroup.addView(RadioButton(this).apply {
                    id = command.ordinal
                    text = getString(command.labelRes)
                    isChecked = command == selection
                })
            }
        } else {
            radioGroup.addView(RadioButton(this).apply {
                id = 0
                text = getString(R.string.tasker_condition_running)
                isChecked = true
            })
        }
        root.addView(radioGroup)

        root.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(Button(context).apply {
                text = getString(R.string.tasker_cancel)
                setOnClickListener { finish() }
            })
            addView(Button(context).apply {
                text = getString(R.string.tasker_save)
                setOnClickListener { save(isSetting) }
            })
        })

        setContentView(root)
    }

    private fun restoreSelection(): Command {
        val bundle = intent.getBundleExtra(PluginContract.EXTRA_BUNDLE) ?: return Command.START
        val json = bundle.getString(PluginContract.EXTRA_STRING_JSON) ?: return Command.START
        return runCatching {
            Command.from(JSONObject(json).optString(PluginContract.KEY_COMMAND))
        }.getOrNull() ?: Command.START
    }

    private fun save(isSetting: Boolean) {
        val command = Command.entries.getOrNull(radioGroup.checkedRadioButtonId) ?: Command.START
        val json = if (isSetting) {
            JSONObject().put(PluginContract.KEY_COMMAND, command.value).toString()
        } else {
            JSONObject().put(PluginContract.KEY_CONDITION, PluginContract.VALUE_CONDITION_RUNNING).toString()
        }
        val blurb = if (isSetting) {
            getString(command.blurbRes)
        } else {
            getString(R.string.tasker_blurb_condition)
        }
        val resultBundle = Bundle().apply {
            putString(PluginContract.EXTRA_STRING_JSON, json)
        }
        val resultIntent = Intent().apply {
            putExtra(PluginContract.EXTRA_BUNDLE, resultBundle)
            putExtra(PluginContract.EXTRA_STRING_BLURB, blurb)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
