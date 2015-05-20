package cs601.blkqueue;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class RingBuffer<T> implements MessageQueue<T> {
	private final AtomicLong w = new AtomicLong(-1);	// just wrote location
	private final AtomicLong r = new AtomicLong(0);		// about to read location
	// not thread safe because these are visible
    private int n;
    private T [] array;
    private int mod=n-1;

	public RingBuffer(int n) {
        if (isPowerOfTwo(n))
            this.n=n;
            array=(T[]) new Object[n];
	}

	// http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetParallel
	static boolean isPowerOfTwo(int v) {
		if (v<0) return false;
		v = v - ((v >> 1) & 0x55555555);                    // reuse input as temporary
		v = (v & 0x33333333) + ((v >> 2) & 0x33333333);     // temp
		int onbits = ((v + (v >> 4) & 0xF0F0F0F) * 0x1010101) >> 24; // count
		// if number of on bits is 1, it's power of two, except for sign bit
		return onbits==1;
	}

	@Override
	public void put(T v) throws InterruptedException {
		// This code makes no sense. It also appears that you are not treating w as "just wrote"
		// here is a proper implementation
		long next = w.get()+1;
		waitForFreeSlotAt(next); 		// after, we own slot w+1
		array[(int)(next & mod)] = v;
		// inc after setting value as reader shouldn't fetch value until then
		w.lazySet(next);   	// with 1 writer, we're ok to incr

//        while (w.intValue()-r.intValue()==(n-1))
//            waitForFreeSlotAt(w.longValue());
//        array[(w.intValue()+1)&(n-1)]=v;
//        w.getAndIncrement();
	}

	@Override
	public T take() throws InterruptedException {
        while (r.intValue()>w.intValue())
            waitForDataAt(r.longValue());
        T tmp=array[r.intValue()&mod];
        r.getAndIncrement();
		return tmp;
	}

	// spin wait instead of lock for low latency store
	void waitForFreeSlotAt(final long writeIndex) {
		// wait until we have at least one spot, meaning w < r
		// since circular buffer though we worry about wrapping. We
		// have to wait if we've got n values in the buffer already.
		while ( writeIndex - r.get() >= n ) {
			// wait since writeIndex has bumped up against readIndex
			//			System.out.println("add waits @ "+writeIndex+", r="+r);
			LockSupport.parkNanos(1L);
		}
	}

	// spin wait instead of lock for low latency store
//	void waitForFreeSlotAt(final long writeIndex) {
//        LockSupport.parkNanos(1);
//	}

	// spin wait instead of lock for low latency pickup
	void waitForDataAt(final long readIndex) {
        LockSupport.parkNanos(1);
	}
}
