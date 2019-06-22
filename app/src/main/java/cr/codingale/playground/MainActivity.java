package cr.codingale.playground;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.List;

import nz.geek.android.things.drivers.adc.I2cAdc;

public class MainActivity extends Activity {
    public static final String TAG = "Android Things Playground:";
    private I2cAdc adc;
    private Handler handler = new Handler();
    private Runnable runnable = new UpdateRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        List<String> devices = PeripheralManager.getInstance().getI2cBusList();
        Log.d(TAG, "I2C Devices: " + devices.toString());
        setupI2C();
    }

    private void setupI2C() {
        I2cAdc.I2cAdcBuilder builder =  I2cAdc.builder();
        adc = builder.address(0).fourSingleEnded().withConversionRate(100).build();
        adc.startConversions();
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class UpdateRunner implements Runnable {
        @Override
        public void run() {
            String s = "";
            for (int i = 0; i < 4; i++) {
                s += " canal " + i + adc.readChannel(i);
            }
            Log.d(TAG, s);
            handler.postDelayed(this, 1000);
        }
    }
}
