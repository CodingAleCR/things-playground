package cr.codingale.playground;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    private TextView tvInformation;

    private static final int LED_INTERVAL = 1000;
    private static final int PORCENTAGE_LED_PWM = 5;
    private static final String BTN_PIN = "BCM21";
    private static final String LED_PIN = "BCM6";
    private static final String RED_LED_RGB_PIN = "BCM5";
    private static final String BLUE_LED_RGB_PIN = "BCM19";
    private static final String GREEN_LED_RGB_PIN = "BCM23";
    private static final String LED_PWM_PIN = "PWM0";
    private Gpio btnGpio;
    private Gpio ledGpio;
    private Pwm ledPWm;
    private Gpio redLedRGBGpio;
    private Gpio greenLedRGBGpio;
    private Gpio blueLedRGBGpio;

    private Handler handler = new Handler();

    private boolean isButtonPressed = false;
    private int pwmLedPercentage = 20;
    private int ledRGBStatus = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInformation = findViewById(R.id.tv_information);

        showGPIOs();
        setButton();
        setLed();
        setPwmLed();
        setLedRGB();

        // Start 1 second repeat handler.
        handler.post(runnable);
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
                refreshLedGpio();
                refreshLedPwm();
                refreshLedRGB();
                handler.postDelayed(runnable, LED_INTERVAL);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void refreshLedGpio() throws IOException {
        if (!isButtonPressed) {
            ledGpio.setValue(!ledGpio.getValue());
        } else {
            ledGpio.setValue(true);
        }
    }

    private void setLed() {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            ledGpio = manager.openGpio(LED_PIN);
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshLedRGB() throws IOException {
        switch (ledRGBStatus) {
            case 1:
                redLedRGBGpio.setValue(true);
                greenLedRGBGpio.setValue(false);
                blueLedRGBGpio.setValue(false);
                break;
            case 2:
                redLedRGBGpio.setValue(false);
                greenLedRGBGpio.setValue(true);
                blueLedRGBGpio.setValue(false);
                break;
            case 3:
                redLedRGBGpio.setValue(false);
                greenLedRGBGpio.setValue(false);
                blueLedRGBGpio.setValue(true);
                break;
            case 4:
                redLedRGBGpio.setValue(true);
                greenLedRGBGpio.setValue(true);
                blueLedRGBGpio.setValue(false);
                break;
            case 5:
                redLedRGBGpio.setValue(false);
                greenLedRGBGpio.setValue(true);
                blueLedRGBGpio.setValue(true);
                break;
            case 6:
                redLedRGBGpio.setValue(true);
                greenLedRGBGpio.setValue(false);
                blueLedRGBGpio.setValue(true);
                break;
            case 7:
                redLedRGBGpio.setValue(true);
                greenLedRGBGpio.setValue(true);
                blueLedRGBGpio.setValue(true);
                break;
            case 8:
                redLedRGBGpio.setValue(false);
                greenLedRGBGpio.setValue(false);
                blueLedRGBGpio.setValue(false);
                break;
        }

        if (ledRGBStatus < 8) {
            ledRGBStatus += 1;
        } else {
            ledRGBStatus = 1;
        }
    }

    private void setLedRGB() {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            redLedRGBGpio = manager.openGpio(RED_LED_RGB_PIN);
            redLedRGBGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            blueLedRGBGpio = manager.openGpio(BLUE_LED_RGB_PIN);
            blueLedRGBGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            greenLedRGBGpio = manager.openGpio(GREEN_LED_RGB_PIN);
            greenLedRGBGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshLedPwm() throws IOException {
        if (pwmLedPercentage <= 80) {
            pwmLedPercentage += 20;
        } else {
            pwmLedPercentage = 0;
        }
        ledPWm.setPwmDutyCycle(pwmLedPercentage);
    }

    private void setPwmLed() {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            ledPWm = manager.openPwm(LED_PWM_PIN);
            ledPWm.setPwmFrequencyHz(120);
            ledPWm.setPwmDutyCycle(PORCENTAGE_LED_PWM);
            ledPWm.setEnabled(true);
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
        if (ledGpio != null) {
            try {
                ledGpio.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (ledPWm != null) {
            try {
                ledPWm.close();
                ledPWm = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
