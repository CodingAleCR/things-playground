package cr.codingale.playground;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;

import java.io.IOException;
import java.util.List;

import static cr.codingale.playground.MainActivity.TAG;

public class MainActivity extends Activity {
    public static final String TAG = "Android Things Playground:";
    private ArduinoUart mArduino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Available UART devices: " + ArduinoUart.availables());
        mArduino = new ArduinoUart("UART0", 115200);

        sendCommand("H");
        sendCommand("D");
    }

    private void sendCommand(String command) {
        String action = "Sending '" + command + "' command...";
        Log.d(TAG, action);

        mArduino.write(command);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String s = mArduino.read();
        String message = "Arduino's response: " + s;

        Log.d(TAG, message);

    }


    @Override
    protected void onDestroy() {
        mArduino.close();
        mArduino=null;
        super.onDestroy();
    }
}

class ArduinoUart {
    private UartDevice uart;

    public ArduinoUart(String name, int baudrate) {
        try {
            uart = PeripheralManager.getInstance().openUartDevice(name);
            uart.setBaudrate(baudrate);
            uart.setDataSize(8);
            uart.setParity(UartDevice.PARITY_NONE);
            uart.setStopBits(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String s) {
        try {
            int writes = uart.write(s.getBytes(), s.length());
            Log.d(TAG, writes + " bytes written in UART");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String read() {
        String s = "";
        int len;
        final int maxCount = 8;
        byte[] buffer = new byte[maxCount];
        try {
            do {
                len = uart.read(buffer, buffer.length);
                for (int i = 0; i < len; i++) {
                    s += (char) buffer[i];
                }
            } while (len > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;
    }

    public void close() {
        if (uart != null) {
            try {
                uart.close();
                uart = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static public List<String> availables() {
        return PeripheralManager.getInstance().getUartDeviceList();
    }
}