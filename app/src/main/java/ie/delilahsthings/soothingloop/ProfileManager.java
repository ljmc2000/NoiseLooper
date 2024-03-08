package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public abstract class ProfileManager {
    final public static String prefix="profile-";
    final public static int prefix_length=prefix.length();
    final public static String suffix=".xml";
    final public static int suffix_length=suffix.length();

    public static String[] listProfiles(Context context)
    {
        String path = Environment.getDataDirectory().getPath()+"/data/"+context.getPackageName()+"/shared_prefs";
        return new File(path).list();
    }

    public static boolean deleteProfile(Context context, String profile)
    {
        String path = Environment.getDataDirectory().getPath()+"/data/"+context.getPackageName()+"/shared_prefs/"+prefix+profile+suffix;
        return new File(path).delete();
    }
}
