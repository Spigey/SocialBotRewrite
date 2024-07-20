package spigey.bot.system;

public class Timer {
    private long startTime = 0L;

    public Timer() {
        this.startTime = System.currentTimeMillis();
    }
    public long end() {
        return System.currentTimeMillis() - this.startTime;
    }
}