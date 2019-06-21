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

public class MainActivity extends Activity {
    public static final String TAG = "Android Things Playground:";
    private static final byte OUT_ACTIVE = 0x40;
    private static final byte AUTOINCREMENT = 0X04;
    private static final byte IN_0 = 0X00;
    private static final byte IN_1 = 0x01;
    private static final byte IN_2 = 0x02;
    private static final byte IN_3 = 0x03;
    private static String IN_I2C_NAME = "I2C1";
    private static final int IN_I2C_DIRECTION = 0x48;
    private I2cDevice i2c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        List<String> devices = PeripheralManager.getInstance().getI2cBusList();
        Log.d(TAG, "I2C Devices: " + devices.toString());
        setupI2C();
    }

    private void setupI2C() {
        try {
            i2c = PeripheralManager.getInstance().openI2cDevice(IN_I2C_NAME, IN_I2C_DIRECTION);

            byte[] config = new byte[2];
            config[0] = OUT_ACTIVE + AUTOINCREMENT;
            config[1] = (byte) 0x80;
            i2c.write(config, config.length);

            byte[] buffer = new byte[5];
            i2c.read(buffer, buffer.length);


            String s = "";
            for (int i = 0; i < buffer.length; i++) {
                s += " byte"+i+": "+ (buffer[i]&0xFF);

            }
            Log.d(TAG, s);
        } catch (IOException e) {
            Log.e(TAG, "setupI2C:", e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            i2c.close();
            i2c = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }
}
