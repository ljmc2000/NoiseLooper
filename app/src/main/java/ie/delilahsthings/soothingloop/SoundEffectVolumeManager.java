package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.SeekBar;

import java.util.HashMap;

public class SoundEffectVolumeManager implements SeekBar.OnSeekBarChangeListener {

    final static int MAX_STREAMS=32;
    private int playbackId=0;
    private float volumeF;
    private int soundPoolIndex;

    private static Runnable onPlayCallback;
    private static FadeOutThread fadeOutThread;
    private static SoundPool soundPool=new SoundPool(SoundEffectVolumeManager.MAX_STREAMS, AudioManager.STREAM_MUSIC,0);
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

    public static void unload(String sound) {
        SoundEffectVolumeManager manager = cache.get(sound);
        soundPool.stop(manager.playbackId);
        soundPool.unload(manager.soundPoolIndex);
        cache.remove(sound);
    }

    public static void stopAll()
    {
        abortFadeout();

        for(SoundEffectVolumeManager manager: cache.values())
        {
            if(manager.playbackId!=0)
            {
                soundPool.stop(manager.playbackId);
                manager.playbackId=0;
            }
        }
    }

    public static void abortFadeout() {
        if(fadeOutThread!=null && fadeOutThread.isAlive()) {
            fadeOutThread.interrupt();
        }
    }

    public static void fadeOut(Context context, long smearLength)
    {
        if(fadeOutThread==null || !fadeOutThread.isAlive()) {
            fadeOutThread = new FadeOutThread(context, smearLength);
            fadeOutThread.start();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int volume, boolean z) {
        abortFadeout();

        volumeF=volume/100f;
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

    static class FadeOutThread extends Thread{
        private Context context;
        private long smearLength;
        public FadeOutThread(Context context, long smearLength)
        {
            this.context=context;
            this.smearLength=smearLength;
        }
        @Override
        public void run()
        {
            long finishAt = System.currentTimeMillis()+smearLength;
            float timeRemaining;

            try {
                while (System.currentTimeMillis() < finishAt) {
                    timeRemaining = finishAt - System.currentTimeMillis();
                    for (SoundEffectVolumeManager manager : cache.values()) {
                        if (manager.playbackId != 0) {
                            manager.volumeF = timeRemaining / smearLength;
                            soundPool.setVolume(manager.playbackId, manager.volumeF, manager.volumeF);
                        }
                    }
                    Thread.sleep(50);
                }
            }
            catch (InterruptedException ex) {
                return;
            }

            for(SoundEffectVolumeManager manager: cache.values())
            {
                if(manager.playbackId!=0) {
                    soundPool.stop(manager.playbackId);
                    manager.playbackId = 0;
                }
            }

            Intent intent = new Intent(Constants.INVALIDATE_ACTION);
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);
        }
    }
}
