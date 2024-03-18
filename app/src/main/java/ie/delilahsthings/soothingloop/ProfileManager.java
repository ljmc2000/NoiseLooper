package ie.delilahsthings.soothingloop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public abstract class ProfileManager {
    final public static String prefix="profile-";
    final public static int prefix_length=prefix.length();
    final public static String suffix=".xml";
    final public static int suffix_length=suffix.length();

    final private static String CUSTOM_SOUNDS = "/custom_sounds/";
    final private static String DATA = "/data/";
    final private static String SHARED_PREFS = "/shared_prefs/";

    static String getProfilePath(Context context)
    {
        return Environment.getDataDirectory().getPath()+DATA+context.getPackageName()+SHARED_PREFS;
    }

    static String getSoundPath(Context context)
    {
        return Environment.getDataDirectory().getPath()+DATA+context.getPackageName()+CUSTOM_SOUNDS;
    }

    public static boolean deleteProfile(Context context, String profile)
    {
        String path = getProfilePath(context)+prefix+profile+suffix;
        return new File(path).delete();
    }

    public static String[] listProfiles(Context context)
    {
        ArrayList<String> files = new ArrayList();
        String path = getProfilePath(context);
        for(String file: new File(path).list()) {
            if (file.startsWith(ProfileManager.prefix))
                files.add(file.substring(ProfileManager.prefix_length, file.length() - ProfileManager.suffix_length));
        }
        return files.toArray(new String[0]);
    }

    public static AddedSoundResult addCustomSound(Context context, Uri uri) throws IOException {
        AddedSoundResult result=new AddedSoundResult();
        String path = getSoundPath(context);
        File dir = new File(path);
        dir.mkdirs();

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File dest = new File(path+getFileName(context,uri));
        dest.createNewFile();
        result.size = copy(inputStream,dest);
        result.name=dest.getName();
        return result;
    }

    public static boolean deleteCustomSound(Context context, String sound)
    {
        String path = getSoundPath(context)+sound;
        return new File(path).delete();
    }

    public static String[] listCustomSounds(Context context)
    {
        String path = getSoundPath(context);
        File dir = new File(path);
        dir.mkdirs();
        return dir.list();
    }

    static int copy(InputStream in, File dst) throws IOException {
        try (OutputStream out = new FileOutputStream(dst)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len, total=0;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                total+=len;
            }
            return total;
        }
    }

    @SuppressLint("Range")
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static class AddedSoundResult
    {
        public String name;
        public int size;
    }
}
