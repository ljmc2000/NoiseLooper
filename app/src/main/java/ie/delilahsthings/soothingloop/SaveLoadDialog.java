package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SaveLoadDialog implements DialogInterface.OnClickListener {

    protected View input;
    protected Listener listener;

    protected SaveLoadDialog(Context context, View input, int title, int actionName, Listener listener)
    {
        this.input=input;
        this.listener=listener;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title);
        builder.setView(input);
        builder.setPositiveButton(actionName,this);
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
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
