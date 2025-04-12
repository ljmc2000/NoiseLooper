package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomSoundsManager {

    static String getSoundPath()
    {
        return StaticContext.getAppContext().getFilesDir()+"/custom_sounds/";
    }

    public static AddedSoundResult addCustomSound(Uri uri) throws IOException {
        Context context=StaticContext.getAppContext();
        AddedSoundResult result=new AddedSoundResult();
        String path = getSoundPath();
        File dir = new File(path);
        dir.mkdirs();

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File dest = new File(path+Util.getFileName(context,uri));
        dest.createNewFile();
        FileOutputStream out = new FileOutputStream(dest);
        result.size = Util.copy(inputStream, out);
        result.name=dest.getName();
        return result;
    }

    public static boolean deleteCustomSound(String sound)
    {
        String path = getSoundPath()+sound;
        return new File(path).delete();
    }

    public static String[] listCustomSounds()
    {
        String path = getSoundPath();
        File dir = new File(path);
        dir.mkdirs();
        return Util.safe_list(dir);
    }

    public static class AddedSoundResult
    {
        public String name;
        public int size;
    }

    //TODO: If after 2026-04-12 then remove this and anything that depends on it
    @Deprecated
    public static boolean migratePre1dot2Noises() {
        String oldSoundPath = Environment.getDataDirectory().getPath()+"/data/"+StaticContext.getAppContext().getPackageName()+"/custom_sounds/";
        String newSoundPath = CustomSoundsManager.getSoundPath();
        File oldSoundDir = new File(oldSoundPath);
        File newSoundDir = new File(newSoundPath);
        File inFile, outFile;
        int count=0;

        if(oldSoundDir.isDirectory()) {
            newSoundDir.mkdir();

            try {
                for (String sound : Util.safe_list(oldSoundDir)) {
                    inFile = new File(oldSoundPath + sound);
                    outFile = new File(newSoundPath + sound);
                    Util.copy(new FileInputStream(inFile), new FileOutputStream(outFile));
                    inFile.delete();
                    count++;
                }

                oldSoundDir.delete();
            } catch (IOException e) {
            }
        }

        return count>0;
    }
}