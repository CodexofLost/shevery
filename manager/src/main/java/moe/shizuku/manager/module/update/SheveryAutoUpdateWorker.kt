package moe.shizuku.manager.module.update

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SheveryAutoUpdateWorker {
    private const val WORK_NAME = "shevery_auto_update_check"

    fun maybeSchedule(context: Context) {
        if (!moe.shizuku.manager.module.ModuleSettings.isAppUpdateAutoCheckEnabled()) {
            return
        }

        val work = PeriodicWorkRequestBuilder<SheveryUpdateCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(10, TimeUnit.MINUTES)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            work
        )
    }
}
