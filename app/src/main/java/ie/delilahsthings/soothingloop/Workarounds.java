package ie.delilahsthings.soothingloop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;

public abstract class Workarounds {

    /**
     * workaround for dialoge buttons having bad colours
     */
    public static void fixBadDialogButtonColours(Context context, AlertDialog dialog)
    {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorForeground, typedValue, true);
        int color = typedValue.data;
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
    }
}
