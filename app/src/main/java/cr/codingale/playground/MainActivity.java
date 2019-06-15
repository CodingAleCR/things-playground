package cr.codingale.playground;

import android.app.Activity;
import android.os.Handler;
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

    private static final int LED_INTERVAL = 1000;
    private static final String BTN_PIN = "BCM21";
    private static final String LED_PIN = "BCM6";
    private Gpio btnGpio;
    private Gpio ledGpio;

    private Handler handler = new Handler();

    private boolean isButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInformation = findViewById(R.id.tv_information);

        showGPIOs();
        setButton();
        setLed();
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
                isButtonPressed = !gpio.getValue();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    private void setButton() {
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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!isButtonPressed) {
                    ledGpio.setValue(!ledGpio.getValue());
                } else {
                    ledGpio.setValue(true);
                }
                handler.postDelayed(runnable, LED_INTERVAL);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void setLed() {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            ledGpio = manager.openGpio(LED_PIN);
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            handler.post(runnable);
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
