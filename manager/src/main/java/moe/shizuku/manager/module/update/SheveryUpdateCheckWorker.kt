package moe.shizuku.manager.module.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SheveryUpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val result = SheveryUpdateChecker.getInstance().checkAppUpdate(applicationContext)
            moe.shizuku.manager.module.ModuleSettings.setAppLastCheckTime(System.currentTimeMillis())
            if (result.hasUpdate && result.downloadUrl != null) {
                Result.success()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
