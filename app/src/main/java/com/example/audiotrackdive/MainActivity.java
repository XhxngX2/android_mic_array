package com.example.audiotrackdive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import static java.lang.Thread.State.NEW;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "AudioDiveIn";

    private AudioRecord mAudioRecorder;

    // should be set by a finding manner
    private static final int SAMPLING_RATE = 44100;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int CHANNEL_IN_TYPE = AudioFormat.CHANNEL_IN_STEREO;
    private static final int ENCODING_TYPE = AudioFormat.ENCODING_PCM_16BIT;

    // audio recorder data related
    private final int mMinBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_TYPE,ENCODING_TYPE);
    private final int mBufferSize = mMinBufferSize *8;
    private final int mPlotDataLength = mMinBufferSize *2;
    private short[] mBufferShort = new short[mPlotDataLength];


    private LineChart mLineChart;
    private ArrayList<Entry> mValues;   //which should related to audio record data

    // monitor thread
    private Thread mMonitorThread;
    private Handler myHandler;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Log.i(TAG, "onCreate:");

        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO}, 12);

        {   // init text view
            TextView tv = findViewById(R.id.sample_text);
            tv.setText(stringFromJNI());
        }

        {   // init Line Chart
            mLineChart = findViewById(R.id.line_chart);
            mLineChart.setDrawGridBackground(false);
            mLineChart.getDescription().setEnabled(false);
            mLineChart.setDrawBorders(false);

            mLineChart.getAxisLeft().setEnabled(true);
            mLineChart.getAxisRight().setDrawAxisLine(true);
            mLineChart.getAxisRight().setDrawGridLines(true);
            mLineChart.getXAxis().setDrawAxisLine(true);
            mLineChart.getXAxis().setDrawGridLines(true);

            // enable touch gestures
            mLineChart.setTouchEnabled(true);

            // enable scaling and dragging
            mLineChart.setDragEnabled(true);
            mLineChart.setScaleEnabled(true);

            Legend l = mLineChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);

            YAxis leftAxis = mLineChart.getAxisLeft();
            leftAxis.setAxisMaximum(40000f);
            leftAxis.setAxisMinimum(-40000f);
            leftAxis.setDrawGridLines(true);
            leftAxis.setGranularityEnabled(true);

            ArrayList<ILineDataSet> tDataSets = new ArrayList<>();

            mValues = new ArrayList<>();

            for (int i = 0; i < mPlotDataLength; i++) {
                mValues.add(new Entry(i, (float) mBufferShort[i]));
            }

            LineDataSet d = new LineDataSet(mValues, "Microphone mono ");
            d.setDrawCircles(false);    // no circle
            d.setMode(LineDataSet.Mode.STEPPED); // stepped
            d.setColor(getResources().getColor(R.color.purple_700));
            d.setLineWidth(2.5f);
            d.setCircleRadius(4f);
            tDataSets.add(d);

            LineData data = new LineData(tDataSets);
            mLineChart.setData(data);
            mLineChart.invalidate();
        }

        // init audio recorder
        mAudioRecorder = new AudioRecord(AUDIO_SOURCE, SAMPLING_RATE, CHANNEL_IN_TYPE, ENCODING_TYPE, mBufferSize);
        mAudioRecorder.startRecording();


        // init thread stuff

        myHandler = new Handler(Looper.getMainLooper(), msg -> {
            if(msg.what == 1){
                LineData data = mLineChart.getData();
                data.notifyDataChanged();
                mLineChart.invalidate();
            }
            return true;
        });

        mMonitorThread = new Thread(()->{

            for(;;) {

                int resultLength = mAudioRecorder.read(mBufferShort, 0, mPlotDataLength);

                for (int i = 0; i < resultLength; i++) {
                    Entry entry = mValues.get(i);
                    entry.setY((float) mBufferShort[i]);
                }

                Message message = new Message();
                message.what = 1;
                myHandler.sendMessage(message);
            }

        });
    }



    public native String stringFromJNI();

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        if(mMonitorThread.getState()== NEW)
            mMonitorThread.start();
        super.onResume();

    }
}