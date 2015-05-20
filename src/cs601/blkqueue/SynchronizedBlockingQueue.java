package cs601.blkqueue;

import java.util.ArrayList;
import java.util.List;

public class SynchronizedBlockingQueue<T> implements MessageQueue<T> {
	// not thread safe because these are visible
    private int size;
    private List<T> list;
    private static int count=0;

	public SynchronizedBlockingQueue(int size) {
        this.size=size;
        list=new ArrayList<T>(size);
	}

	@Override
	public synchronized void put(T o) throws InterruptedException {
		while(count == size) {
			wait();
		}
		count++;
		list.add(o);
		notifyAll();
	}

	@Override
	public synchronized T take()throws InterruptedException {
		//synchronized (this) { // why not just synchronize the method?
			while (count == 0) {
				wait();
			}
			count--;
			T tmp=list.get(0);
			list.remove(0);
			notifyAll();
			return tmp;
		//}
	}
}
