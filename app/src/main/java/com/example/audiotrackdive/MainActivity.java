package com.example.audiotrackdive;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AudioDiveIn";
    private AudioTrack mAudioTrack;
    private AudioRecord mAudioRecord;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO}, 12);

        PackageManager tPackageManager = this.getPackageManager();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);

        int[] tSampleRates = new int[]{8000,11025,22050,44100};
        short[] tAudioFormat = new short[]{AudioFormat.ENCODING_PCM_8BIT,AudioFormat.ENCODING_PCM_16BIT,AudioFormat.ENCODING_PCM_FLOAT};
        short[] tChannelConfig = new short[]{AudioFormat.CHANNEL_IN_MONO,AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.CHANNEL_IN_BACK|AudioFormat.CHANNEL_IN_FRONT,AudioFormat.CHANNEL_IN_STEREO|AudioFormat.CHANNEL_IN_BACK|AudioFormat.CHANNEL_IN_FRONT};

/*
        for (int rate : tSampleRates) {
            for (int format : tAudioFormat) {
                for (int chanel_config : tChannelConfig) {
                    try {

                        int buffer_size = AudioRecord.getMinBufferSize(rate, chanel_config, format);
                        if (buffer_size == AudioRecord.ERROR_BAD_VALUE) {
                            Log.d(TAG, "onCreate: AudioRecorder Bad config value:" + rate + "Hz, chanel config:" +
                                    chanel_config + ",format:" + format);
                        } else {
                            //    Log.d(TAG, "current buffer size:"+buffer_size+",with rate of "+rate+",chanel config" +
                            //            ":"+chanel_config+",format:"+format);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Wrong config", e);
                    }
                }
            }
        }

        */


        int mBufferSizeInBytes = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_STEREO|AudioFormat.CHANNEL_IN_BACK|AudioFormat.CHANNEL_IN_FRONT
                ,AudioFormat.ENCODING_PCM_16BIT
        );

        AudioRecord mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_STEREO|AudioFormat.CHANNEL_IN_BACK|AudioFormat.CHANNEL_IN_FRONT,
                AudioFormat.ENCODING_PCM_16BIT,
                mBufferSizeInBytes);

        short[] data = new short[mBufferSizeInBytes*2];
/*
        mAudioRecord.startRecording();

        while(true) {
            int resultLength = mAudioRecord.read(data, 0, mBufferSizeInBytes * 2);
            Log.d(TAG, "captured: "+resultLength+"samples");
        }

*/

    }

    

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}