package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.content.Intent;

public class SleepTimerThread  extends Thread{
    static SleepTimerThread singleton = null;

    public static volatile Context context;
    private volatile long remainingTime=-1;

    private SleepTimerThread()
    {
    }

    @Override
    public synchronized void run()
    {
        Intent intent;

        while(remainingTime>=0) {
            if (context != null) {
                intent = new Intent(Constants.TIMER_EVENT);
                intent.putExtra(Constants.REMAINING_TIME, remainingTime);
                intent.setPackage(context.getPackageName());
                context.sendBroadcast(intent);
            }

            remainingTime--;
            Util.sleep(1000);
        }

        singleton=null;
    }

    public static synchronized void setTime(long timeout)
    {
        if(singleton==null) {
            singleton = new SleepTimerThread();
            singleton.remainingTime=timeout;
            singleton.start();
        }
        else {
            singleton.remainingTime=timeout;
        }
    }

    public static void subscribe(Context context)
    {
        SleepTimerThread.context=context;
    }

}
