package cr.codingale.playground;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    private TextView tvInformation;

    private static final String BTN_PIN = "BCM21";
    private Gpio btnGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInformation = findViewById(R.id.tv_information);

        showGPIOs();
        setGpioBtns();
    }

    private void showGPIOs() {
        PeripheralManager manager = PeripheralManager.getInstance();
        StringBuilder ios = new StringBuilder();
        for (String io : manager.getGpioList()) {
            ios.append(io).append("\n");
        }

        tvInformation.setText(ios.toString());
    }

    private GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.e(TAG, "onGpioEdge: Button change " + Boolean.toString(gpio.getValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    private void setGpioBtns() {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            btnGpio = manager.openGpio(BTN_PIN);
            btnGpio.setDirection(Gpio.DIRECTION_IN);
            btnGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            btnGpio.registerGpioCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (btnGpio != null) {
            btnGpio.unregisterGpioCallback(callback);
            try {
                btnGpio.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
