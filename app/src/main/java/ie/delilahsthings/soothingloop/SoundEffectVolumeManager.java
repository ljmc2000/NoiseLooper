package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.media.SoundPool;
import android.widget.SeekBar;

public class SoundEffectVolumeManager implements SeekBar.OnSeekBarChangeListener {

    private int playbackId=-1;
    private int soundPoolIndex;
    private SoundPool soundPool;

    public SoundEffectVolumeManager(Context context, SoundPool soundPool, int soundId) {
        this.soundPool=soundPool;
        this.soundPoolIndex=soundPool.load(context, soundId, 1);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int volume, boolean b) {
        float volumeF=volume/100f;
        if(playbackId==-1) {
            if (volume != 0) {
                playbackId=soundPool.play(soundPoolIndex, volumeF, volumeF, 1, -1, 1f);
            }
        }
        else if(volume==0) {
            soundPool.stop(playbackId);
            playbackId=-1;
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
