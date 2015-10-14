package lachlanrobinson.myfirstapp;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class MyActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    // initialise required variables for serial communication
    SerialReadData readResult = new SerialReadData(-1, new byte[16]);
    private static UsbSerialPort sPort = null;
    UsbManager manager;
    List<UsbSerialDriver> availableDrivers;

    UsbSerialDriver driver;
    UsbDeviceConnection connection;


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
        StartSerial();
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
                message = "L9";
                writeToSerial(message.getBytes());
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

    public void sendMessage(View view) {
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        byte[] byteMessage = message.getBytes();
        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);
        writeToSerial(byteMessage);

    }

    private void writeToSerial(byte[] letter) {
        byte[] sendable = new byte[letter.length + 1];
        for (int i = 0; i < letter.length; i++) {
            sendable[i] = letter[i];
        }
        sendable[letter.length] = 0x0A;

        //remove spurious line endings so the serial device doesn't get confused
        for (int i = 0; i < sendable.length - 1; i++) {
            if (sendable[i] == 0x0A) {
                sendable[i] = 0x0B;
            }
        }

        try {

            sPort.write(sendable, 500);

        } catch (IOException e) {
            // Deal with error.
            try {
                sPort.close();
                Toast.makeText(this, "Serial Error - Closed", Toast.LENGTH_LONG).show();
            } catch (IOException e2) {
                // Ignore.
            }
            sPort = null;
            Toast.makeText(this, "Serial Error - Port set to Null", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private SerialReadData readFromSerial() {

        int numBytesRead;
        byte buffer[] = new byte[16];

        try {

            numBytesRead = sPort.read(buffer, 500);

        } catch (IOException e) {
            // Deal with error.
            try {
                sPort.close();
                Toast.makeText(this, "Serial Error - Closed", Toast.LENGTH_LONG).show();
            } catch (IOException e2) {
                // Ignore.
            }
            sPort = null;
            Toast.makeText(this, "Serial Error - Port set to Null", Toast.LENGTH_LONG).show();
            return new SerialReadData(-1, new byte[16]);
        }

        String message = "";
        for (int i = 0; i < buffer.length; i++) {
            message = message + Byte.toString(buffer[i]);
        }
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(Integer.toString(numBytesRead) + " " + message);
        setContentView(textView);
        return new SerialReadData(numBytesRead, buffer);
    }

    public void StartSerial() {

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {

            Toast.makeText(this, "Serial Error", Toast.LENGTH_LONG).show();
        } else {

            driver = availableDrivers.get(0);
            connection = manager.openDevice(driver.getDevice());

            if (connection == null) {

                Toast.makeText(this, "Serial Error 2", Toast.LENGTH_LONG).show();

            } else {

                try {

                    sPort = driver.getPorts().get(0);
                    sPort.open(connection);
                    sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    Toast.makeText(this, "Serial OK!", Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    // Deal with error.
                    try {
                        sPort.close();
                        Toast.makeText(this, "Serial Error - opening Closed", Toast.LENGTH_LONG).show();
                    } catch (IOException e2) {
                        // Ignore.
                    }
                    sPort = null;
                    Toast.makeText(this, "Serial Error - opening Port set to Null", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }
}
