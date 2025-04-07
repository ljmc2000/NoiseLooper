package ie.delilahsthings.soothingloop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public abstract class Util {

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

    public static <T> T getChildOfType(ViewGroup viewGroup, Class<T> type)
    {
        int numberOfChildren = viewGroup.getChildCount();

        for (int i = 0; i < numberOfChildren; i++) {
            View child = viewGroup.getChildAt(i);

            if (type.isAssignableFrom(child.getClass())) {
                return type.cast(child);
            }
        }

        return null;
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

    //TODO: If after 2026-04-07 then remove this and anything that depends on it
    @Deprecated
    public static boolean migratePre1dot2Noises() {
        String oldSoundPath = Environment.getDataDirectory().getPath()+"/data/"+StaticContext.getAppContext().getPackageName()+"/custom_sounds/";
        String newSoundPath = CustomSoundsManager.getSoundPath();
        File oldSoundDir = new File(oldSoundPath);
        File newSoundDir = new File(newSoundPath);
        File inFile, outFile;

        if(oldSoundDir.isDirectory()) {
            if (!newSoundDir.mkdir()) {
                return false;
            }

            try {
                for (String sound : safe_list(oldSoundDir)) {
                    inFile = new File(oldSoundPath + sound);
                    outFile = new File(newSoundPath + sound);
                    copy(new FileInputStream(inFile), new FileOutputStream(outFile));
                    inFile.delete();
                }

                oldSoundDir.delete();
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    //TODO: If after 2026-04-07 then remove this and anything that depends on it
    @Deprecated
    public static boolean migratePre1dot2Profiles() {
        String oldProfilePath = Environment.getDataDirectory().getPath()+"/data/"+StaticContext.getAppContext().getPackageName()+"/shared_prefs/";
        String newProfilePath = ProfileManager.getProfilePath();
        File oldProfileDir = new File(oldProfilePath);
        File newProfileDir = new File(newProfilePath);
        File inFile, outFile;
        newProfileDir.mkdirs();

        if(!oldProfileDir.isDirectory()) {
            return true;
        }

        try {
            inFile = new File(oldProfilePath + "default.xml");
            outFile = ProfileManager.getDefaultProfileFile();

            if(inFile.exists()) {
                copy(new FileInputStream(inFile), new FileOutputStream(outFile));
                inFile.delete();
            }

            for (String profile : safe_list(oldProfileDir)) {
                if(profile.startsWith("profile-")) {
                    inFile = new File(oldProfilePath + profile);
                    outFile = new File(newProfilePath + profile.substring(8));
                    copy(new FileInputStream(inFile), new FileOutputStream(outFile));
                    inFile.delete();
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * run function unless it has been run at any point previously
     * @param prefs object to check and store whether function has run previously
     * @param func function, if returns true make not that function ran successfully
     * @param pref key to lookup previous success in prefs
     */
    public static void run_once(SharedPreferences prefs, Callable<Boolean> func, String pref) {
        try {
            if (!prefs.getBoolean(pref, false)) {
                Boolean success = func.call();
                if(success) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(pref, true);
                    editor.apply();
                }
            }
        }
        catch (Exception ex) {
        }
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

    static void sleep(long milis)
    {
        try {
            Thread.sleep(milis);
        }
        catch (InterruptedException e) {
        }
    }
}
