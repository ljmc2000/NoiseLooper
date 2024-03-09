package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.media.SoundPool;
import android.widget.SeekBar;

import java.io.File;

public class SoundEffectVolumeManager implements SeekBar.OnSeekBarChangeListener {

    private int playbackId=0;
    private int soundPoolIndex;
    public static SoundPool soundPool;

    public SoundEffectVolumeManager(Context context, int soundId) {
        this.soundPoolIndex=soundPool.load(context, soundId, 1);
    }

    public SoundEffectVolumeManager(String sound) {
        this.soundPoolIndex=soundPool.load(sound, 1);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int volume, boolean z) {
        float volumeF=volume/100f;
        if(playbackId==0) {
            if (volume != 0) {
                for(int i=0; i<10; i++) {
                    playbackId = soundPool.play(soundPoolIndex, volumeF, volumeF, 1, -1, 1f);
                    if(playbackId!=0)
                        return;
                    try{
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                    }
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

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
