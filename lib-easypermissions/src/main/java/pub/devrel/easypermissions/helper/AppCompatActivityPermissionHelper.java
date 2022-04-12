package pub.devrel.easypermissions.helper;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

/**
 * Permissions helper for {@link AppCompatActivity}.
 */
class AppCompatActivityPermissionHelper extends BaseSupportPermissionsHelper<AppCompatActivity> {

    public AppCompatActivityPermissionHelper(AppCompatActivity host) {
        super(host);
    }

    @Override
    public FragmentManager getSupportFragmentManager() {
        return getHost().getSupportFragmentManager();
    }

    @Override
    public void directRequestPermissions(int requestCode, @NonNull String... perms) {
        ActivityCompat.requestPermissions(getHost(), perms, requestCode);
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String perm) {
        return ActivityCompat.shouldShowRequestPermissionRationale(getHost(), perm);
    }

    @Override
    public Context getContext() {
        return getHost();
    }
}
