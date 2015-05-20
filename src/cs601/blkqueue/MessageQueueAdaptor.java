package cs601.blkqueue;

import java.util.concurrent.ArrayBlockingQueue;

class MessageQueueAdaptor<T> implements MessageQueue<T> {
 //not thread safe
    int size;
    ArrayBlockingQueue<T> arrayBlockingQueue;

    MessageQueueAdaptor(int size){
        this.size=size;
        arrayBlockingQueue=new ArrayBlockingQueue<T>(size);
    }

	@Override
	public void put(T o) throws InterruptedException{
        arrayBlockingQueue.put(o);
	}

	@Override
	public T take() throws InterruptedException {
		return arrayBlockingQueue.take();
	}
}
