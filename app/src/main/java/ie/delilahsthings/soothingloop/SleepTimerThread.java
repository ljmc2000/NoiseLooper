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

    private synchronized void updateTimer()
    {
        remainingTime--;
    }

    @Override
    public void run()
    {
        Intent intent;

        while(remainingTime>=0) {
            if (context != null) {
                intent = new Intent(Constants.TIMER_EVENT);
                intent.putExtra(Constants.REMAINING_TIME, remainingTime);
                intent.setPackage(context.getPackageName());
                context.sendBroadcast(intent);
            }

            updateTimer();
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

    public static synchronized void subscribe(Context context)
    {
        SleepTimerThread.context=context;
    }

}
