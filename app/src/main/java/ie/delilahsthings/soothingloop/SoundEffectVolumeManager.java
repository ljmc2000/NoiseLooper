package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.media.SoundPool;
import android.widget.SeekBar;

public class SoundEffectVolumeManager implements SeekBar.OnSeekBarChangeListener {

    private int playbackId=0;
    private int soundPoolIndex;
    private SoundPool soundPool;

    public SoundEffectVolumeManager(Context context, SoundPool soundPool, int soundId) {
        this.soundPool=soundPool;
        this.soundPoolIndex=soundPool.load(context, soundId, 1);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int volume, boolean z) {
        float volumeF=volume/100f;
        if(playbackId==0) {
            if (volume != 0) {
                playbackId=soundPool.play(soundPoolIndex, volumeF, volumeF, 1, -1, 1f);
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
