package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

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
            CustomSoundsManager.copy(inputStream, out);
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
            return CustomSoundsManager.copy(in, out)>0;
        }
        catch (IOException ex) {
            return false;
        }
    }

    public static String[] listProfiles()
    {
        ArrayList<String> files = new ArrayList();
        String path = getProfilePath();
        for(String file: Util.safe_list(new File(path))) {
            if (file.startsWith(ProfileManager.prefix))
                files.add(file.substring(ProfileManager.prefix_length, file.length() - ProfileManager.suffix_length));
        }
        return files.toArray(new String[0]);
    }

    public static String validateProfileName(CharSequence profileName) throws BadProfileNameException
    {
        Matcher matcher = profileNameValidationPattern.matcher(profileName);
        if(matcher.find())
            return matcher.group(1);
        else
            throw new BadProfileNameException();
    }

    public static class BadProfileNameException extends Exception {
        public BadProfileNameException() {
            super("Invalid Profile Name");
        }
    }
}
