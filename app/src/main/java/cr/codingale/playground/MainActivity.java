package cr.codingale.playground;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.PeripheralManager;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    private TextView tvInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInformation = findViewById(R.id.tv_information);

        showGPIOs();
    }

    private void showGPIOs() {
        PeripheralManager manager = PeripheralManager.getInstance();
        StringBuilder ios = new StringBuilder();
        for (String io : manager.getGpioList()) {
            ios.append(io).append("\n");
        }

        tvInformation.setText(ios.toString());
    }
}
