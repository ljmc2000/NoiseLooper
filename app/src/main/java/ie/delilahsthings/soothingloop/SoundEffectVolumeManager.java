package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.SeekBar;

import java.util.HashMap;
import java.util.Map;

public class SoundEffectVolumeManager implements SeekBar.OnSeekBarChangeListener {

    final static int MAX_STREAMS=32;
    private int playbackId=0;
    private float volumeF, fadeStart;
    private int soundPoolIndex;

    private static Runnable onPlayCallback;
    private static FadeSmearThread fadeSmearThread;
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
        abortFade();

        for(SoundEffectVolumeManager manager: cache.values())
        {
            if(manager.playbackId!=0)
            {
                soundPool.stop(manager.playbackId);
                manager.playbackId=0;
            }
        }
    }

    public static void abortFade() {
        if(fadeSmearThread!=null && fadeSmearThread.isAlive()) {
            fadeSmearThread.interrupt();
        }
    }

    public static void fadeIn(Context context, long smearLength)
    {
        if(fadeSmearThread==null || !fadeSmearThread.isAlive()) {
            fadeSmearThread = new FadeInThread(context, smearLength);
            fadeSmearThread.start();
        }
    }
    public static void fadeOut(Context context, long smearLength)
    {
        if(fadeSmearThread==null || !fadeSmearThread.isAlive()) {
            fadeSmearThread = new FadeOutThread(context, smearLength);
            fadeSmearThread.start();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int volume, boolean z) {
        abortFade();

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

    static class FadeInThread extends FadeSmearThread{
        public FadeInThread(Context context, long smearLength)
        {
            super(context, smearLength);
            afterFade.putExtra(Constants.FADE_TYPE, Constants.FADE_IN);
        }

        protected float calculateVolumeForTimeRemaining(float fadeStart, float timeRemaining) {
            return 1 - (fadeStart * (timeRemaining / smearLength));
        }
    }

    static class FadeOutThread extends FadeSmearThread{
        public FadeOutThread(Context context, long smearLength)
        {
            super(context, smearLength);
            afterFade.putExtra(Constants.FADE_TYPE, Constants.FADE_OUT);
        }

        protected float calculateVolumeForTimeRemaining(float fadeStart, float timeRemaining) {
            return fadeStart * (timeRemaining / smearLength);
        }
    }

    static abstract class FadeSmearThread extends Thread{
        protected Context context;
        protected long smearLength;
        Intent afterFade = new Intent(Constants.FADE_COMPLETED_ACTION);

        protected abstract float calculateVolumeForTimeRemaining(float fadeStart, float timeRemaining);

        protected FadeSmearThread(Context context, long smearLength)
        {
            this.context=context;
            this.smearLength=smearLength;
        }
        @Override
        public void run()
        {
            afterFade.setPackage(context.getPackageName());

            long finishAt = System.currentTimeMillis()+smearLength;
            float timeRemaining;

            HashMap<String, Float> startVolumes = new HashMap<>();
            for (SoundEffectVolumeManager manager : cache.values()) {
                manager.fadeStart=manager.volumeF;
            }

            try {
                while (System.currentTimeMillis() < finishAt) {
                    timeRemaining = finishAt - System.currentTimeMillis();
                    for (SoundEffectVolumeManager manager : cache.values()) {
                        if (manager.playbackId != 0) {
                            manager.volumeF = calculateVolumeForTimeRemaining(manager.fadeStart, timeRemaining);
                            soundPool.setVolume(manager.playbackId, manager.volumeF, manager.volumeF);
                        }
                    }
                    Thread.sleep(50);
                }
            }
            catch (InterruptedException ex) {
                afterFade.putExtra(Constants.FADE_INTERRUPTED, true);
                context.sendBroadcast(afterFade);
                return;
            }

            for(SoundEffectVolumeManager manager: cache.values())
            {
                if(manager.playbackId!=0) {
                    soundPool.stop(manager.playbackId);
                    manager.playbackId = 0;
                }
            }

            context.sendBroadcast(afterFade);
        }
    }
}
