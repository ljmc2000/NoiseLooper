package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.SeekBar;

import java.util.HashMap;

public class SoundEffectVolumeManager implements SeekBar.OnSeekBarChangeListener {

    final static int MAX_STREAMS=32;
    private int playbackId=0;
    private int soundPoolIndex;
    private static Runnable onPlayCallback;
    private static SoundPool soundPool=new SoundPool(SoundEffectVolumeManager.MAX_STREAMS, AudioManager.STREAM_MUSIC,0);;
    private static HashMap<String,SoundEffectVolumeManager> cache = new HashMap<>();
    public static boolean EVER_PLAYED=false;

    private SoundEffectVolumeManager(Context context, int soundId) {
        this.soundPoolIndex=soundPool.load(context, soundId, 1);
    }

    private SoundEffectVolumeManager(String sound) {
        this.soundPoolIndex=soundPool.load(sound, 1);
    }

    public static SoundEffectVolumeManager get(Context context, String persistKey, int soundId) {
        if(cache.containsKey(persistKey)) {
            return cache.get(persistKey);
        }
        else {
            SoundEffectVolumeManager manager = new SoundEffectVolumeManager(context, soundId);
            cache.put(persistKey,manager);
            return manager;
        }
    }
    public static SoundEffectVolumeManager get(String sound){
        if(cache.containsKey(sound)) {
            return cache.get(sound);
        }
        else {
            SoundEffectVolumeManager manager = new SoundEffectVolumeManager(sound);
            cache.put(sound,manager);
            return manager;
        }
    }

    public static void stopAll(SeekBar seekbar)
    {
        for(SoundEffectVolumeManager manager: cache.values())
        {
            manager.onProgressChanged(seekbar,0,false);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int volume, boolean z) {
        float volumeF=volume/100f;
        if(playbackId==0) {
            if (volume != 0) {
                for(int i=0; i<10; i++) {
                    playbackId = soundPool.play(soundPoolIndex, volumeF, volumeF, 1, -1, 1f);
                    if(playbackId!=0) {
                        onPlayCallback.run();
                        EVER_PLAYED=true;
                        return;
                    }
                    Util.sleep(500);
                }

                seekBar.setProgress(0);
            }
        }
        else if(volume==0) {
            soundPool.stop(playbackId);
            playbackId=0;
        }
        else {
            soundPool.setVolume(playbackId, volumeF, volumeF);
        }
    }

    public static void setOnPlayCallback(Runnable onPlayCallback)
    {
        SoundEffectVolumeManager.onPlayCallback=onPlayCallback;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
