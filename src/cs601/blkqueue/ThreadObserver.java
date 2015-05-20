package cs601.blkqueue;

import java.util.concurrent.locks.LockSupport;
import java.util.HashMap;
import java.util.Map;

/** A runnable class that attaches to another thread and wakes up
 *  at regular intervals to determine that thread's state. The goal
 *  is to figure out how much time that thread is blocked, waiting,
 *  or sleeping.
 */
class ThreadObserver implements Runnable {

    public static final long MONITORING_PERIOD = 1000L;
    protected final Map<String, Long> histogram = new HashMap<String, Long>();
    protected int numEvents = 0;
    protected int blocked = 0;
    protected int waiting = 0;
    protected int sleeping = 0;
    protected boolean done = false;
    protected final Thread threadToMonitor;
    protected long periodInNanoSeconds;

    public ThreadObserver(Thread threadToMonitor, long periodInNanoSeconds) {
        this.threadToMonitor=threadToMonitor;
        this.periodInNanoSeconds=periodInNanoSeconds;
	}

    @Override
    public void run() {
        while (!done) {
            numEvents++;
            StackTraceElement[] stackTraceElement=threadToMonitor.getStackTrace();
			// stackTraceElement is not guaranteed to have elements
                if (!histogram.containsKey(stackTraceElement[0].getClassName()+"."+stackTraceElement[0].getMethodName())) {
                    histogram.put(stackTraceElement[0].getClassName() + "." + stackTraceElement[0].getMethodName(), 1L);
                }else {
                    histogram.put(stackTraceElement[0].getClassName() + "." + stackTraceElement[0].getMethodName(),histogram.get(stackTraceElement[0].getClassName() + "." + stackTraceElement[0].getMethodName())+1);
                }
            switch ( threadToMonitor.getState() ) {
                case BLOCKED: blocked++; break;
                case WAITING: waiting++; break;
                case TIMED_WAITING: sleeping++; break;
            }
            LockSupport.parkNanos(MONITORING_PERIOD);
        }
    }
    public Map<String, Long> getMethodSamples() {return histogram;}

    public void terminate() { done = true; }

    public String toString() {
        return String.format("(%d blocked + %d waiting + %d sleeping) / %d samples = %1.2f%% wasted",
                blocked,
                waiting,
                sleeping,
                numEvents,
                100.0*(blocked + waiting + sleeping)/numEvents);
    }
}
