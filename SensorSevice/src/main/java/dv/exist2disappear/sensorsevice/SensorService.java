package dv.exist2disappear.sensorsevice;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import dv.dyq.sensor.acc.GravityData;

/**
 * Created by orange on 16-3-26.
 */
public class SensorService extends Service {

    public final static String TAG = "SensorService";
    public final static String ATION_START = "dv.dyq.sensor.action.senser.start";
    public final static String ATION_END = "dv.dyq.sensor.action.senser.end";


    private SensorManager sm = null;
    private GravityData dataForSignalProcess=new GravityData();

    private boolean isTesting = false;
    private static int timeCnt=0;

    private Timer timeOnButton=null;
    private TimerTask timeCntTask=null;

    @Override
    public void onCreate() {
        super.onCreate();
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] values = event.values;
                if(isTesting)
                    dataForSignalProcess.popData(values[0],values[1],values[2],timeCnt);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }
                , sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                , SensorManager.SENSOR_DELAY_GAME);
        timeOnButton=new Timer();
        timeCntTask=new TimerTask(){
            public void run()
            {timeCnt++;}
        };
        timeOnButton.schedule(timeCntTask, 20, 20);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ATION_START.equals(intent.getAction())){
            isTesting = true;
            dataForSignalProcess.initDataBeforeTest();
            timeCnt = 0;

        }
        if(ATION_END.equals(intent.getAction())){
            if(isTesting)
            {
                isTesting = false;
                DecimalFormat dataFormat=new DecimalFormat("#.##");
                Log.i(TAG, "x:" + dataFormat.format(dataForSignalProcess.getLongX()));
                Log.i(TAG, "y:" + dataFormat.format(dataForSignalProcess.getLongX()));
                Log.i(TAG, "z:" + dataFormat.format(dataForSignalProcess.getLongX()));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

}
