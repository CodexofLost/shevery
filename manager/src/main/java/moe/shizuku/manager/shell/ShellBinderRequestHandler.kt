package moe.shizuku.manager.shell

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import moe.shizuku.manager.utils.Logger.LOGGER
import rikka.shizuku.Shizuku

object ShellBinderRequestHandler {

    private val pkgManager by lazy { context.getSystemService(Context.PACKAGE_MANAGER_SERVICE) as PackageManager }

    fun handleRequest(context: Context, intent: Intent): Boolean {
        if (intent.action != "rikka.shizuku.intent.action.REQUEST_BINDER") {
            return false
        }

        // Verify the calling package holds the API_V23 permission before granting access
        val callingUid = Binder.getCallingUid()
        if (callingUid != Process.SYSTEM_UID) {
            val packageName = pkgManager.getPackageName(callingUid, 0)
            if (packageName != null) {
                val perm = "${context.packageName}.permission.API_V23"
                if (pkgManager.checkPermission(perm, packageName) != PackageManager.PERMISSION_GRANTED) {
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
