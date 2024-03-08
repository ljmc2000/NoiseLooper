package ie.delilahsthings.soothingloop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;

public class SaveLoadDialog implements DialogInterface.OnClickListener {

    protected View input;
    protected Listener listener;

    protected SaveLoadDialog(Context context, View input, int title, int actionName, Listener listener)
    {
        this.input=input;
        this.listener=listener;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(input);
        builder.setPositiveButton(actionName,this);
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();

        //workaround for both buttons having bad colours
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorForeground, typedValue, true);
        int color = typedValue.data;
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        String filename="default";
        if(input instanceof TextView) {
            filename = ProfileManager.prefix + ((TextView)input).getText().toString();
        }
        else if(input instanceof Spinner) {
            filename = ProfileManager.prefix + ((Spinner)input).getSelectedItem().toString();
        }
        listener.action(filename);
    }

    public interface Listener {
        void action(String filename);
    }
}
