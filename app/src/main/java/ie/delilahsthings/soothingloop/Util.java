package ie.delilahsthings.soothingloop;

import android.view.View;
import android.view.ViewGroup;

public abstract class Util {
    static void sleep(long milis)
    {
        try {
            Thread.sleep(milis);
        }
        catch (InterruptedException e) {
        }
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
}
