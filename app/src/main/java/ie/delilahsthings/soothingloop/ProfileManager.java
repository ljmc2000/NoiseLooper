package ie.delilahsthings.soothingloop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static boolean importProfile(String profile, Uri uri) {
        Context ctx=StaticContext.getAppContext();
        String path = getProfilePath();
        File dir = new File(path);
        dir.mkdirs();
        String destPath = path+prefix+profile+suffix;

        try {
            InputStream inputStream = ctx.getContentResolver().openInputStream(uri);
            File dest = new File(destPath);
            dest.createNewFile();
            FileOutputStream out = new FileOutputStream(dest);
            copy(inputStream, out);
            return true;
        }
        catch (IOException ex) {
            return false;
        }
    }

    public static boolean exportProfile(String profile, Uri uri) {
        String srcPath = getProfilePath()+prefix+profile+suffix;
        String destPath = uri.getPath();
        if(destPath==null) {
            return false;
        }

        try {
            Context ctx = StaticContext.getAppContext();
            InputStream in = new FileInputStream(srcPath);
            OutputStream out = ctx.getContentResolver().openOutputStream(uri);
            return copy(in, out)>0;
        }
        catch (IOException ex) {
            return false;
        }
    }

    public static String[] listProfiles()
    {
        ArrayList<String> files = new ArrayList();
        String path = getProfilePath();
        for(String file: safe_list(new File(path))) {
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
        return safe_list(dir);
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

    public static String[] safe_list(File dir) {
        String[] lst = dir.list();
        if(lst==null){
            return new String[0];
        }
        else {
            return lst;
        }
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
