package ie.delilahsthings.soothingloop;

import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

public class CheckboxBooleanToggle {
    private CompoundButton box;
    private Runnable callback;
    private String field;
    private SharedPreferences settings;

    private CheckboxBooleanToggle(SharedPreferences settings, String field, CompoundButton box, Runnable callback)
    {
        this.settings=settings;
        this.field=field;
        this.box=box;
        this.callback=callback;
    }

    public void onClickBox(CompoundButton box, boolean checked)
    {
        SharedPreferences.Editor editor=settings.edit();
        editor.putBoolean(field,checked);
        editor.commit();
        callback.run();
    }

    public void onClickOther(View sender)
    {
        boolean checked = !box.isChecked();
        box.toggle();
        onClickBox(box,checked);
    }

    public static void build(SharedPreferences settings, String field, ViewGroup viewGroup, Runnable callback)
    {
        CompoundButton box = Util.getChildOfType(viewGroup, CompoundButton.class);
        CheckboxBooleanToggle setting = new CheckboxBooleanToggle(settings, field, box, callback);
        box.setChecked(settings.getBoolean(field, false));
        box.setOnCheckedChangeListener((box_,checked)->setting.onClickBox(box_,checked));
        viewGroup.setOnClickListener((view)->setting.onClickOther(view));
    }

    public static void build(SharedPreferences settings, String field, ViewGroup viewGroup)
    {
        build(settings, field, viewGroup, ()->{});
    }
}
