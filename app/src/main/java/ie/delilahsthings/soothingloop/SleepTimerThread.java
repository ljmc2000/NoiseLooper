package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.content.Intent;

public class SleepTimerThread  extends Thread{
    static SleepTimerThread singleton = new SleepTimerThread();

    private Context context;
    private long remainingTime=-1;

    private SleepTimerThread()
    {
        start();
    }

    @Override
    public void run()
    {
        Intent intent;

        while(true)
        {
            if(remainingTime>=0) {
                if (context != null) {
                    intent = new Intent(Constants.TIMER_EVENT);
                    intent.putExtra(Constants.REMAINING_TIME, remainingTime);
                    intent.setPackage(context.getPackageName());
                    context.sendBroadcast(intent);
                }

                remainingTime--;
            }

            Util.sleep(1000);
        }
    }

    public void setTime(long timeout)
    {
        this.remainingTime=timeout;
    }

    public void subscribe(Context context)
    {
        this.context=context;
    }

    public static SleepTimerThread get()
    {
        return singleton;
    }
}
