package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public abstract class ProfileManager {
    final public static String prefix="profile-";
    final public static int prefix_length=prefix.length();
    final public static String suffix=".xml";
    final public static int suffix_length=suffix.length();

    public static String[] listProfiles(Context context)
    {
        ArrayList<String> files = new ArrayList();
        String path = Environment.getDataDirectory().getPath()+"/data/"+context.getPackageName()+"/shared_prefs";
        for(String file: new File(path).list()) {
            if (file.startsWith(ProfileManager.prefix))
                files.add(file.substring(ProfileManager.prefix_length, file.length() - ProfileManager.suffix_length));
        }
        return files.toArray(new String[0]);
    }

    public static boolean deleteProfile(Context context, String profile)
    {
        String path = Environment.getDataDirectory().getPath()+"/data/"+context.getPackageName()+"/shared_prefs/"+prefix+profile+suffix;
        return new File(path).delete();
    }
}
