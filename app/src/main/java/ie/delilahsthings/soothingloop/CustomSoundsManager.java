package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        result.size = copy(inputStream, out);
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

    static int copy(InputStream in, OutputStream out) throws IOException {
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len, total=0;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
            total+=len;
        }
        return total;
    }

    public static class AddedSoundResult
    {
        public String name;
        public int size;
    }
}