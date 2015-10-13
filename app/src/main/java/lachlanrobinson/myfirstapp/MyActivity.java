package lachlanrobinson.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDeviceConnection;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.lang.String;
import java.lang.Byte;

public class MyActivity extends AppCompatActivity {

    int readResult = -1;

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";
    private static UsbSerialPort sPort = null;

    public void sendMessage(View view) {
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        byte[] byteMessage = message.getBytes();
        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);
        writetoserial(byteMessage);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the text view as the activity layout
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText("Read Result");
        //setContentView(textView);
        setContentView(R.layout.activity_my);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        int[] letters = {97, 98, 99, 100, 37, 38};
        String message;

        switch (id) {
            case R.id.action_settings:
                return true;

            case R.id.send_serial:
                message = "a";
                writetoserial(message.getBytes());
                return true;

            case R.id.read_serial:
                readResult = readFromSerial();
                return true;

            case R.id.change_text:
                TextView textView = new TextView(this);
                textView.setTextSize(40);
                textView.setText("Text Changed!");
                setContentView(textView);

                return true;

            case R.id.reset:
                setContentView(R.layout.activity_my);
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }


    }

    private void writetoserial(byte[] letter) {
        byte[] sendable = new byte[letter.length+1];
        for (int i=0; i<letter.length; i++){
            sendable[i] = letter[i];
        }
        sendable[letter.length] = 0x0A;

        //remove spurious line endings so the serial device doesn't get confused
        for (int i=0; i<sendable.length-1; i++){
            if (sendable[i] == 0x0A){
                sendable[i] = 0x0B;
            }
        }

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());

        if (connection == null) {
            return;
        }

        try {
            sPort = driver.getPorts().get(0);
            sPort.open(connection);
            sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            sPort.write(sendable, 500);
        } catch (IOException e) {
            // Deal with error.
            try {
                sPort.close();
            } catch (IOException e2) {
                // Ignore.
            }
            sPort = null;
            return;
        }
    }

    private int readFromSerial() {

        int numBytesRead;

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            TextView textView = new TextView(this);
            textView.setTextSize(40);
            textView.setText(Integer.toString(-1));
            setContentView(textView);
            return -1;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());

        if (connection == null) {
            TextView textView = new TextView(this);
            textView.setTextSize(40);
            textView.setText(Integer.toString(-1));
            setContentView(textView);
            return -1;
        }

        try {
            sPort = driver.getPorts().get(0);
            sPort.open(connection);
            sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            byte buffer[] = new byte[16];
            numBytesRead = sPort.read(buffer, 500);
        } catch (IOException e) {
            // Deal with error.
            try {
                sPort.close();
            } catch (IOException e2) {
                // Ignore.
            }
            sPort = null;
            return -1;
        }

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(Integer.toString(numBytesRead));
        setContentView(textView);
        return numBytesRead;


    }
}
