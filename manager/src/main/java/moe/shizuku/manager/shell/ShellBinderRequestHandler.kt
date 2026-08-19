package moe.shizuku.manager.shell

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.Process
import android.os.Parcel
import android.util.Log
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.utils.Logger.LOGGER
import rikka.shizuku.Shizuku

object ShellBinderRequestHandler {

    fun handleRequest(context: Context, intent: Intent): Boolean {
        if (intent.action != "rikka.shizuku.intent.action.REQUEST_BINDER") {
            return false
        }

        // Verify the calling package holds the API_V23 permission before granting access
        val callingUid = Binder.getCallingUid()
        if (callingUid != Process.SYSTEM_UID) {
            val pm = context.packageManager
            val packages = pm.getPackagesForUid(callingUid)
            if (packages != null && packages.isNotEmpty()) {
                val packageName = packages[0]
                val perm = "${context.packageName}.permission.API_V23"
                if (pm.checkPermission(perm, packageName) != PackageManager.PERMISSION_GRANTED) {
                    Log.w(AppConstants.TAG, "Denied binder request from $packageName (no API_V23)")
                    return false
                }
            }
        }

        val binder = intent.getBundleExtra("data")?.getBinder("binder") ?: return false
        val shizukuBinder = Shizuku.getBinder()
        if (shizukuBinder == null) {
            LOGGER.w("Binder not received or Shizuku service not running")
        }

        val data = Parcel.obtain()
        return try {
            data.writeStrongBinder(shizukuBinder)
            data.writeString(context.applicationInfo.sourceDir)
            binder.transact(1, data, null, IBinder.FLAG_ONEWAY)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        } finally {
            data.recycle()
        }
    }
}
