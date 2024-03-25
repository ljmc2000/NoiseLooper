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
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ProfileManager {
    final public static String prefix="profile-";
    final public static int prefix_length=prefix.length();
    final public static String suffix=".xml";
    final public static int suffix_length=suffix.length();

    final private static String CUSTOM_SOUNDS = "/custom_sounds/";
    final private static String DATA = "/data/";
    final private static String SHARED_PREFS = "/shared_prefs/";

    final private static Pattern profileNameValidationPattern = Pattern.compile("\\s*(.*[\\S])\\s*");

    static String getProfilePath()
    {
        return Environment.getDataDirectory().getPath()+DATA+StaticContext.getAppContext().getPackageName()+SHARED_PREFS;
    }

    static String getSoundPath()
    {
        return Environment.getDataDirectory().getPath()+DATA+StaticContext.getAppContext().getPackageName()+CUSTOM_SOUNDS;
    }

    public static boolean deleteProfile(String profile)
    {
        String path = getProfilePath()+prefix+profile+suffix;
        return new File(path).delete();
    }

    public static String[] listProfiles()
    {
        ArrayList<String> files = new ArrayList();
        String path = getProfilePath();
        for(String file: new File(path).list()) {
            if (file.startsWith(ProfileManager.prefix))
                files.add(file.substring(ProfileManager.prefix_length, file.length() - ProfileManager.suffix_length));
        }
        return files.toArray(new String[0]);
    }

    public static AddedSoundResult addCustomSound(Uri uri) throws IOException {
        Context context=StaticContext.getAppContext();
        AddedSoundResult result=new AddedSoundResult();
        String path = getSoundPath();
        File dir = new File(path);
        dir.mkdirs();

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File dest = new File(path+getFileName(context,uri));
        dest.createNewFile();
        result.size = copy(inputStream,dest);
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

    public static String validateProfileName(CharSequence profileName) throws BadProfileNameException
    {
        Matcher matcher = profileNameValidationPattern.matcher(profileName);
        if(matcher.find())
            return matcher.group(1);
        else
            throw new BadProfileNameException();
    }

    public static class AddedSoundResult
    {
        public String name;
        public int size;
    }

    public static class BadProfileNameException extends Exception {
        public BadProfileNameException() {
            super("Invalid Profile Name");
        }
    }
}
