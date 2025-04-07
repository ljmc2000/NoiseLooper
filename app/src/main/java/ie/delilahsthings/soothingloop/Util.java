package ie.delilahsthings.soothingloop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
