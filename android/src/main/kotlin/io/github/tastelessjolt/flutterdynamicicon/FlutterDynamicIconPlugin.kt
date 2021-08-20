package io.github.tastelessjolt.flutterdynamicicon

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.github.tastelessjolt.flutterdynamicicon.FlutterDynamicIconPlugin
import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterDynamicIconPlugin  */
class FlutterDynamicIconPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var context: Context
    private lateinit var activity: Activity

    companion object {
        private const val CHANNEL_NAME = "flutter_dynamic_icon"

        // This static function is optional and equivalent to onAttachedToEngine. It supports the old
        // pre-Flutter-1.12 Android projects.
        fun registerWith(registrar: Registrar) {
            val plugin = FlutterDynamicIconPlugin()
            plugin.setupChannel(registrar.messenger(), registrar.context())
        }
    }

    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        setupChannel(binding.binaryMessenger, binding.applicationContext)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        teardownChannel()
    }

    private fun setupChannel(messenger: BinaryMessenger, context: Context) {
        channel = MethodChannel(messenger, CHANNEL_NAME)
        channel.setMethodCallHandler(this)
        this.context = context
    }

    private fun teardownChannel() {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "mSupportsAlternateIcons" -> {
                result.success(true)
            }
            "mGetAlternateIconName" -> {
                result.error("Not supported", "Not supported on Android", null)
            }
            "mSetAlternateIconName" -> {
                val iconName = call.argument<String>("iconName")
                if (!iconName.isNullOrBlank())
                    changeIcon(iconName)
                else
                    result.error("Missing argument", "iconName is required", null)
            }
            "mGetApplicationIconBadgeNumber" -> {
                result.error("Not supported", "Not supported on Android", null)
            }
            "mSetApplicationIconBadgeNumber" -> {
                result.error("Not supported", "Not supported on Android", null)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {}

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}

    override fun onDetachedFromActivity() {}

    private fun changeIcon(iconName: String) {
        val manager = this.context.packageManager
        manager.setComponentEnabledSetting(
            ComponentName(
                activity,
                context.packageName.plus(".MainActivity")
            ),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        manager.setComponentEnabledSetting(
            ComponentName(
                activity,
                context.packageName.plus(".$iconName")
            ), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}