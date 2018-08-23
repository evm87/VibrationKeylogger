package app.myandroidhello.com.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Sensor;
import android.widget.Chronometer;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private String buttonPressed = "N/A";

    //For handling external storage permissions
    private int requestCode;
    private int grantResults[];

    //For labeling multiple sensorEvent instances during individual button presses as one unique event. Used when converting to MLI format in Weka.
    private int instanceID = 0;

    //Array of string[] to be added to the csv file row by row
    List<String[]> data = new ArrayList<String[]>();

    //Default column labels for CSV file.
    String[] columnLabels = "Instance#Button#Time#aX#aY#aZ#gX#gY#gZ".split("#");

    //Data to be written to CSV file
    float aX;
    float aY;
    float aZ;

    String accelX = null;
    String accelY = null;
    String accelZ = null;

    float gX;
    float gY;
    float gZ;

    String gyroX = null;
    String gyroY = null;
    String gyroZ = null;

    long AccelTime;
    long GyroTime;

    private SensorManager sensorManager;

    private Sensor accelerometer;
    private Sensor gyroscope;

    private ButtonView buttonView;

    private Chronometer timer;
    long millisec;

    String csv = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/rawData.csv";
    CSVWriter writer;

    public MainActivity()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonView = (ButtonView) findViewById(R.id.buttons);
        buttonView.setMainActivity(this);

        timer = (Chronometer) findViewById(R.id.timer);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED )
        {
            //Ask for permission to write to external storage
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
            onRequestPermissionsResult(requestCode, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, grantResults);
        }

        try
        {
            if (isExternalStorageWritable())
            {

                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);

                System.out.println("CSV Location: " + csv);

                writer = new CSVWriter(new FileWriter(csv));

                writer.writeNext(columnLabels);

            } else {
                System.out.println("External storage not available!");
            }
        }
        catch (IOException e)
        {
            System.out.println("IOException Occurred...................................................................................");
            e.printStackTrace();
            System.out.println(".......................................................................................................");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    /**
     * Android recommended class to handle permissions
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("permission", "granted");
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();

                    //App cannot function without this permission for now so close it
                    onDestroy();
                }
            }
        }
    }

    @Override
    /**
     * Runs every time a user taps the screen
     */
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = Math.round(event.getX());
        int y = Math.round(event.getY());

        // MotionEvent object holds X-Y values. This section runs the moment a finger touches the screen.
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            this.instanceID = this.instanceID + 1;
            try {
                if (buttonView.button1.contains(x, y)) {
                    this.buttonPressed = "1";
                } else if (buttonView.button2.contains(x, y)) {
                    this.buttonPressed = "2";
                } else if (buttonView.button3.contains(x, y)) {
                    this.buttonPressed = "3";
                } else if (buttonView.button4.contains(x, y)) {
                    this.buttonPressed = "4";
                } else if (buttonView.button5.contains(x, y)) {
                    this.buttonPressed = "5";
                } else if (buttonView.button6.contains(x, y)) {
                    this.buttonPressed = "6";
                } else if (buttonView.button7.contains(x, y)) {
                    this.buttonPressed = "7";
                } else if (buttonView.button8.contains(x, y)) {
                    this.buttonPressed = "8";
                } else if (buttonView.button9.contains(x, y)) {
                    this.buttonPressed = "9";
                } else if (buttonView.button0.contains(x, y)) {
                    this.buttonPressed = "0";
                } else if (buttonView.buttonSubmit.contains(x, y)) {
                    sensorManager.unregisterListener(MainActivity.this, accelerometer);
                    sensorManager.unregisterListener(MainActivity.this, gyroscope);
                    writer.close();
                    onDestroy();
                }
            }
            catch (Exception e)
            {
                //If a file cannot be written to, the user is notified.
                Toast.makeText(MainActivity.this, "File Not Written", Toast.LENGTH_SHORT).show();
            }
        }

        //Runs when a user removes a finger from the screen
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            this.buttonPressed = "N/A";
            this.instanceID = this.instanceID + 1;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Runs every time a sensor detects movement
     * @param sensorEvent
     */
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        Sensor mySensor = sensorEvent.sensor;

        long curTime = System.currentTimeMillis();

        millisec = SystemClock.elapsedRealtime() - timer.getBase();

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            aX = sensorEvent.values[0];
            aY = sensorEvent.values[1];
            aZ = sensorEvent.values[2];

            AccelTime = curTime;

            accelX = String.valueOf(aX);
            accelY = String.valueOf(aY);
            accelZ = String.valueOf(aZ);

        }

        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            GyroTime = curTime;

            gX = sensorEvent.values[0];
            gY = sensorEvent.values[1];
            gZ = sensorEvent.values[2];

            gyroX = String.valueOf(gX);
            gyroY = String.valueOf(gY);
            gyroZ = String.valueOf(gZ);

            //To prevent double entries in the CSV file, data is only recorded during the gyroscope logic after the accelerometer data is stored
            if (AccelTime == GyroTime)
            {

                data.add(new String[]{"press" + String.valueOf(this.instanceID), this.buttonPressed, String.valueOf(millisec), accelX, accelY, accelZ, gyroX, gyroY, gyroZ});
                writer.writeAll(data);
                data.clear();
            }

        }

    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onResume()
    {
        super.onResume();
    }

    /**
     * Unregister the sensor listeners if the app is paused
     */
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     * Unreegister the listeners if the app is stopped
     */
    protected void onStop()
    {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }

    /**
     *  Checks if external storage is available for read and write
     *  */
    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

}