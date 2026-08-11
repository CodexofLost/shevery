package moe.shizuku.privileged.api;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class ForwardActivity extends Activity {

    public static final String SHEVERY_PACKAGE = "com.hamondev.shevery";
    public static final String REQUEST_PERMISSION_ACTIVITY =
            "moe.shizuku.manager.authorization.RequestPermissionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (!isSheveryInstalled()) {
                finish();
                return;
            }

            Intent intent = new Intent(getIntent());
            intent.setPackage(SHEVERY_PACKAGE);
            intent.setComponent(new ComponentName(SHEVERY_PACKAGE, REQUEST_PERMISSION_ACTIVITY));
            intent.setFlags(0);

            startActivity(intent);
        } catch (Throwable ignored) {
        }
        finish();
    }

    private boolean isSheveryInstalled() {
        try {
            getPackageManager().getPackageInfo(SHEVERY_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}