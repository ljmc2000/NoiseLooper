package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.content.Intent;

public class SleepTimerThread  extends Thread{
    static SleepTimerThread singleton = null;
    private volatile long endTime=-1;

    private SleepTimerThread()
    {
    }

    private synchronized long getRemainingTime()
    {
        return endTime-System.currentTimeMillis();
    }

    @Override
    public void run()
    {
        Intent intent;
        long remainingTime;
        Context context;

        do {
            remainingTime = getRemainingTime();
            context=StaticContext.getAppContext();

            if (context != null) {
                intent = new Intent(Constants.TIMER_EVENT);
                intent.putExtra(Constants.REMAINING_TIME, remainingTime/1000);
                intent.setPackage(context.getPackageName());
                context.sendBroadcast(intent);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }

        } while(remainingTime>=0);

        singleton=null;
    }

    public static synchronized boolean isRunning()
    {
        return singleton!=null;
    }

    public static synchronized void setTime(long timeoutInSeconds)
    {
        if(singleton==null) {
            singleton = new SleepTimerThread();
            singleton.endTime=System.currentTimeMillis()+(1000*timeoutInSeconds);
            singleton.start();
        }
        else {
            singleton.endTime=System.currentTimeMillis()+(1000*timeoutInSeconds);
        }
    }

    public static synchronized void stopSleepTimer()
    {
        if(singleton!=null) {
            singleton.interrupt();
            singleton = null;
        }
    }
}
