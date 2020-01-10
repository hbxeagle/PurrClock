package www.hbx.name.purrclock;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 0) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                startService(new Intent(MainActivity.this, FloatingClockService.class));
            }
        }
    }

    public void startFloatingClockService(View view) {
        if (FloatingClockService.isStarted) {
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
        } else {
            Intent intent = new Intent(MainActivity.this, FloatingClockService.class);
            intent.putExtra("action", "show");
            startService(intent);
        }
    }

    public void stopFloatingClockService(View view) {
        if (!FloatingClockService.isStarted) {
            return;
        }

        Intent intent = new Intent(MainActivity.this, FloatingClockService.class);
        intent.putExtra("action", "hide");
        startService(intent);
    }

    public void switchFloatingClockPattern(View view) {
        Intent intent = new Intent(MainActivity.this, FloatingClockService.class);
        String type;
        switch (view.getId()) {
            case R.id.switch_year:
                type = "year";
                break;
            case R.id.switch_hrs:
                type = "hrs";
                break;
            case R.id.switch_sec:
                type = "sec";
                break;
            case R.id.switch_btr:
                type = "btr";
                break;
            default:
                type = "";
                break;
        }

        intent.putExtra("action", type);
        startService(intent);
    }
}
