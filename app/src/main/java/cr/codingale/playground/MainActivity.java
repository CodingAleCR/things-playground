package cr.codingale.playground;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {
    public static final String TAG = "Android Things Playground:";
    private static final String ECHO_PIN_NAME = "BCM20";
    private static final String TRIGGER_PIN_NAME = "BCM16";
    private static int READ_INTERVAL = 3000;
    private Gpio mEcho;
    private Gpio mTrigger;
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                readDistance();
                handler.postDelayed(this, READ_INTERVAL);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            mEcho = manager.openGpio(ECHO_PIN_NAME);
            mEcho.setDirection(Gpio.DIRECTION_IN);
            mEcho.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mEcho.setActiveType(Gpio.ACTIVE_HIGH);

            mTrigger = manager.openGpio(TRIGGER_PIN_NAME);
            mTrigger.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            handler.post(runnable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int doSomething;

    protected void readDistance() throws IOException, InterruptedException {
        // Just to be sure, set the trigger first to false
        mTrigger.setValue(false);
        Thread.sleep(0, 2000);

        // Hold the trigger pin HIGH for at least 10 us
        mTrigger.setValue(true);
        Thread.sleep(0, 10000); //10 microsec

        // Reset the trigger pin
        mTrigger.setValue(false);

        // Wait for pulse on ECHO pin
        while (!mEcho.getValue()) {
            //long t1 = System.nanoTime();
            //Log.d(TAG, "Echo has not arrived...");

            // keep the while loop busy
            doSomething = 0;

            //long t2 = System.nanoTime();
            //Log.d(TAG, "diff 1: " + (t2-t1));
        }
        long time1 = System.nanoTime();
        Log.i(TAG, "Echo ARRIVED!");

        // Wait for the end of the pulse on the ECHO pin
        while (mEcho.getValue()) {
            //long t1 = System.nanoTime();
            //Log.d(TAG, "Echo is still coming...");

            // keep the while loop busy
            doSomething = 1;

            //long t2 = System.nanoTime();
            //Log.d(TAG, "diff 2: " + (t2-t1));
        }
        long time2 = System.nanoTime();
        Log.i(TAG, "Echo ENDED!");

        // Measure how long the echo pin was held high (pulse width)
        long pulseWidth = time2 - time1;

        // Calculate distance in centimeters. The constants
        // are coming from the datasheet, and calculated from the assumed speed
        // of sound in air at sea level (~340 m/s).
        double distance = (pulseWidth / 1000.0) / 58.23; //cm

        // or we could calculate it withe the speed of the sound:
        //double distance = (pulseWidth / 1000000000.0) * 340.0 / 2.0 * 100.0;

        Log.i(TAG, "distance: " + distance + " cm");
    }

    @Override
    protected void onDestroy() {
        try {
            mEcho.close();
            mTrigger.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }
}
