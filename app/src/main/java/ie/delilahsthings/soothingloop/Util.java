package ie.delilahsthings.soothingloop;

public abstract class Util {
    static void sleep(long milis)
    {
        try {
            Thread.sleep(milis);
        }
        catch (InterruptedException e) {
        }
    }
}
