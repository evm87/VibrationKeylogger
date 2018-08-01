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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Sensor;
import android.widget.Chronometer;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import static org.apache.commons.math3.transform.DftNormalization.STANDARD;


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
    //Was pressID#Button#etc
    String[] columnLabels = ("Button#aXMean#aYMean#aZMean#gXMean#gYMean#gZMean#kAX#kAY#kAZ#kGX#kGY#kGZ#sAX#sAY#sAZ#sGX#sGY#sGZ#" +
                             "aXMin#aXMax#aYMin#aYMax#aZMin#aZMax#gXMin#gXMax#gYMin#gYMax#gZMin#gZMax#ac-1Norm#gy-1norm#ac-infNorm#" +
                             "gy-infNorm#ac-frobNorm#gy-frobNorm").split("#");

    //Data to be written to CSV file
    float aX;
    float aY;
    float aZ;

    double accelX;
    double accelY;
    double accelZ;

    float gX;
    float gY;
    float gZ;

    double gyroX;
    double gyroY;
    double gyroZ;

    List<Double> allAX = new ArrayList<>();
    List<Double> allAY = new ArrayList<>();
    List<Double> allAZ = new ArrayList<>();

    List<Double> allGX = new ArrayList<>();
    List<Double> allGY = new ArrayList<>();
    List<Double> allGZ = new ArrayList<>();

    long AccelTime;
    long GyroTime;

    private SensorManager sensorManager;

    private Sensor accelerometer;
    private Sensor gyroscope;

    private ButtonView buttonView;

    private Chronometer timer;
    long millisec;

    String csv = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/data.csv";
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
/*Uncomment out when ready to figure out IOException*/
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

    @Override // android recommended class to handle permissions
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("permission", "granted");
                } else {
                    //Permission denied. Disable the functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();

                    //App cannot function without this permission for now so close it
                    onDestroy();
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = Math.round(event.getX());
        int y = Math.round(event.getY());

        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            this.instanceID = this.instanceID + 1;
            try {
                if (buttonView.button1.contains(x, y)) {
                    this.buttonPressed = "1";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button2.contains(x, y)) {
                    this.buttonPressed = "2";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button3.contains(x, y)) {
                    this.buttonPressed = "3";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button4.contains(x, y)) {
                    this.buttonPressed = "4";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button5.contains(x, y)) {
                    this.buttonPressed = "5";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button6.contains(x, y)) {
                    this.buttonPressed = "6";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button7.contains(x, y)) {
                    this.buttonPressed = "7";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button8.contains(x, y)) {
                    this.buttonPressed = "8";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button9.contains(x, y)) {
                    this.buttonPressed = "9";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.button0.contains(x, y)) {
                    this.buttonPressed = "0";
                    //System.out.println("Press detected.........press #" + this.instanceID + ", " + buttonPressed);
                } else if (buttonView.buttonSubmit.contains(x, y)) {
                    sensorManager.unregisterListener(MainActivity.this, accelerometer);
                    sensorManager.unregisterListener(MainActivity.this, gyroscope);
                    writer.close();
                    onDestroy();
                }
            }
            catch (Exception e)
            {
                Toast.makeText(MainActivity.this, "File Not Written", Toast.LENGTH_SHORT).show();
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            //Create mean of all accel and gyro data and convert to strings
            String aXMean = Double.toString(calcMean(allAX));
            String aYMean = Double.toString(calcMean(allAY));
            String aZMean = Double.toString(calcMean(allAZ));
            String gXMean = Double.toString(calcMean(allGX));
            String gYMean = Double.toString(calcMean(allGY));
            String gZMean = Double.toString(calcMean(allGZ));

            //Create kurtosis of all accel/gyro data
            String kAX = Double.toString(calcKurtosis(allAX));
            String kAY = Double.toString(calcKurtosis(allAY));
            String kAZ = Double.toString(calcKurtosis(allAZ));
            String kGX = Double.toString(calcKurtosis(allGX));
            String kGY = Double.toString(calcKurtosis(allGY));
            String kGZ = Double.toString(calcKurtosis(allGZ));

            //Create skewness of all accel/gyro data
            String sAX = Double.toString(calcSkewness(allAX));
            String sAY = Double.toString(calcSkewness(allAY));
            String sAZ = Double.toString(calcSkewness(allAZ));
            String sGX = Double.toString(calcSkewness(allGX));
            String sGY = Double.toString(calcSkewness(allGY));
            String sGZ = Double.toString(calcSkewness(allGZ));

            //Get min/max values
            double aXMin = Collections.min(allAX);
            double aXMax = Collections.max(allAX);
            double aYMin = Collections.min(allAY);
            double aYMax = Collections.max(allAY);
            double aZMin = Collections.min(allAZ);
            double aZMax = Collections.max(allAZ);
            double gXMin = Collections.min(allGX);
            double gXMax = Collections.max(allGX);
            double gYMin = Collections.min(allGY);
            double gYMax = Collections.max(allGY);
            double gZMin = Collections.min(allGZ);
            double gZMax = Collections.max(allGZ);

            //Calculate 1-Norm (max sum between the rows)
            String accel_one_norm = String.valueOf(calc1Norm(allAX, allAY, allAZ));
            String gyro_one_norm = String.valueOf(calc1Norm(allGX, allGY, allGZ));

            //Calculate infinity norm (max sum between the columns)
            String accel_inf_norm = String.valueOf(calcInfNorm(allAX, allAY, allAZ));
            String gyro_inf_norm = String.valueOf(calcInfNorm(allGX, allGY, allGZ));

            //Calculate Frobenius (Euclidean) norm (square root of the squared sum of all entries in matrix)
            String accel_frob_norm = String.valueOf(calcFrobeniusNorm(allAX, allAY, allAZ));
            String gyro_frob_norm = String.valueOf(calcFrobeniusNorm(allGX, allGY, allGZ));

            //Add all values to a String[] and write to the csv file
            data.add(new String[]{this.buttonPressed, aXMean, aYMean, aZMean, gXMean, gYMean, gZMean, kAX, kAY, kAZ, kGX, kGY, kGZ,
                                    sAX, sAY, sAZ, sGX, sGY, sGZ, String.valueOf(aXMin), String.valueOf(aXMax), String.valueOf(aYMin), String.valueOf(aYMax), String.valueOf(aZMin), String.valueOf(aZMax),
                                    String.valueOf(gXMin), String.valueOf(gXMax), String.valueOf(gYMin), String.valueOf(gYMax), String.valueOf(gZMin), String.valueOf(gZMax), accel_one_norm, gyro_one_norm,
                                    accel_inf_norm, gyro_inf_norm, accel_frob_norm, gyro_frob_norm});

            writer.writeAll(data);

            data.clear();
            allAX.clear();
            allAY.clear();
            allAZ.clear();
            allGX.clear();
            allGY.clear();
            allGZ.clear();

            this.buttonPressed = "N/A";
            this.instanceID = this.instanceID + 1;

        }
        return super.onTouchEvent(event);
    }

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

            if(!buttonPressed.equals("N/A"));
            {
                accelX = (double)aX;
                accelY = (double)aY;
                accelZ = (double)aZ;

                allAX.add(accelX);
                allAY.add(accelY);
                allAZ.add(accelZ);
            }
        }

        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            GyroTime = curTime;

            gX = sensorEvent.values[0];
            gY = sensorEvent.values[1];
            gZ = sensorEvent.values[2];

            if(!buttonPressed.equals("N/A"));
            {
                gyroX = (double)gX;
                gyroY = (double)gY;
                gyroZ = (double)gZ;

                allGX.add(gyroX);
                allGY.add(gyroY);
                allGZ.add(gyroZ);
            }

            if (AccelTime == GyroTime)
            {

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

    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onStop()
    {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }

    /* Checks if external storage is available for read and write */
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
    public boolean onOptionsItemSelected(MenuItem item)
    {

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * Calculates mean value of a list of variables. Uses Apache Commons.
     * @param vals
     * @return
     */
    public double calcMean(List<Double> vals)
    {
        //Convert arrayList<double> to double[]
        double[] values = new double[vals.size()];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = vals.get(i);
        }

        Mean mean = new Mean();
        double theMeanValue = mean.evaluate(values, 0, values.length);

        return theMeanValue;
    }

    /**
     * Calculates kurtosis of a list of values. Uses Apache Commons.
     * @param vals
     * @return
     */
    public double calcKurtosis(List<Double> vals)
    {
        double[] values = new double[vals.size()];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = vals.get(i);
        }

        Kurtosis kurtosis = new Kurtosis();
        double theKurtosisValue = kurtosis.evaluate(values, 0, values.length);

        return theKurtosisValue;
    }

    /**
     * Calculates the skewness of a list of values. Uses Apache Commons.
     * @param vals
     * @return
     */
    public double calcSkewness(List<Double> vals)
    {
        double[] values = new double[vals.size()];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = vals.get(i);
        }

        Skewness skewness = new Skewness();
        double theSkewnessValue = skewness.evaluate(values, 0, values.length);

        return theSkewnessValue;
    }

    /**
     * Calculate the 1-norm of a matrix of accelerometer and gyroscope events
     * @param XEvents
     * @param YEvents
     * @param ZEvents
     * @return
     */
    public double calc1Norm(List<Double> XEvents, List<Double> YEvents, List<Double> ZEvents)
    {
        double one_norm = 0;

        List<Double> allXAbs = new ArrayList<>();
        List<Double> allYAbs = new ArrayList<>();
        List<Double> allZAbs = new ArrayList<>();

        for (int i = 0; i < XEvents.size(); i++)
        {
            allXAbs.add(Math.abs(XEvents.get(i)));
        }

        for (int i = 0; i < YEvents.size(); i++)
        {
            allYAbs.add(Math.abs(YEvents.get(i)));
        }

        for (int i = 0; i < ZEvents.size(); i++)
        {
            allZAbs.add(Math.abs(ZEvents.get(i)));
        }

        for(int i = 0; i < allXAbs.size(); i++)
        {
            double rowValue;
            rowValue = allXAbs.get(i) + allYAbs.get(i) + allZAbs.get(i);

            if (rowValue > one_norm)
            {
                one_norm = rowValue;
            }

        }

        return one_norm;
    }

    /**
     * Calculate the infinity norm of a matrix column.
     * @param allX
     * @param allY
     * @param allZ
     * @return
     */
    public double calcInfNorm(List<Double> allX, List<Double> allY, List<Double> allZ)
    {
        double infNorm;

        List<Double> allXAbs = new ArrayList<>();
        List<Double> allYAbs = new ArrayList<>();
        List<Double> allZAbs = new ArrayList<>();

        for (int i = 0; i < allX.size(); i++)
        {
            allXAbs.add(Math.abs(allX.get(i)));
        }

        for (int i = 0; i < allY.size(); i++)
        {
            allYAbs.add(Math.abs(allY.get(i)));
        }

        for (int i = 0; i < allZ.size(); i++)
        {
            allZAbs.add(Math.abs(allZ.get(i)));
        }

        double xValue = 0;
        for(int i = 0; i < allXAbs.size(); i++)
        {
            xValue = xValue + allXAbs.get(i);
        }

        infNorm = xValue;

        double yValue = 0;
        for(int i = 0; i < allYAbs.size(); i++)
        {
            yValue = yValue + allYAbs.get(i);
        }

        if (infNorm < yValue)
        {
            infNorm = yValue;
        }

        double zValue = 0;
        for(int i = 0; i < allZAbs.size(); i++)
        {
            zValue = zValue + allZAbs.get(i);
        }

        if(infNorm < zValue)
        {
            infNorm = zValue;
        }

        return infNorm;
    }

    /**
     * Calulated the Frobenius norm of a matrix.
     */
    public double calcFrobeniusNorm(List<Double> xEvents, List<Double> yEvents, List<Double> zEvents)
    {
        double frobNorm = 0;

        for(int i = 0; i < xEvents.size(); i++)
        {
            double xEventSquared = xEvents.get(i) * xEvents.get(i);
            double yEventSquared = yEvents.get(i) * yEvents.get(i);
            double zEventSquared = zEvents.get(i) * zEvents.get(i);

            frobNorm = frobNorm + xEventSquared + yEventSquared + zEventSquared;
        }

        frobNorm = Math.sqrt(frobNorm);

        return frobNorm;
    }

    /**
     * Calculates polynomial spline function of two sets of x, y coordinates with Apache Commons (unused)
     * @param xVals
     * @param yVals
     * @return
     */
    public PolynomialSplineFunction calcSplineInt(List<Double> xVals, List<Double> yVals)
    {
        double[] xValues = new double[xVals.size()];
        double[] yValues = new double[yVals.size()];

        for (int i = 0; i < xValues.length; i++)
        {
            xValues[i] = xVals.get(i);
        }

        for (int i = 0; i < yValues.length; i++)
        {
            yValues[i] = yVals.get(i);
        }

        SplineInterpolator spline = new SplineInterpolator();

        PolynomialSplineFunction theSplineFunction = spline.interpolate(xValues, yValues);

        return theSplineFunction;
    }

    /**
     * Calculates FFT of a data set(unused)
     * @param vals
     * @return
     */
    public Complex[] calcFFT(double[] vals)
    {
        FastFourierTransformer FFT = new FastFourierTransformer(STANDARD);
        Complex[] valsArray = FFT.transform(vals, TransformType.FORWARD);

        return valsArray;
    }

    /**
     * Calculates how much padding is needed in an array for FFT (unused)
     * @param length
     * @return
     */
    public int getPadValue(int length)
    {
        int highestOneBit = Integer.highestOneBit(length);
        if (length == highestOneBit)
        {
            return length;
        }
        return highestOneBit << 1;
    }

}