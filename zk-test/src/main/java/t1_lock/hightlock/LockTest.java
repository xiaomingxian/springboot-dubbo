package t1_lock.hightlock;


import t1_lock.AbstractLock;
import t1_lock.simplelock.SimpleZkLock;

public class LockTest {
    public static void main(String[] args) {
        //模拟多个10个客户端
        for (int i=0;i<10;i++) {
            Thread thread = new Thread(new LockRunnable());
            thread.start();
        }

    }

    static class LockRunnable implements Runnable{

        @Override
        public void run() {
            AbstractLock zkLock = new HighPerformanceZkLock();
            //AbstractLock zkLock = new HighPerformanceZkLock();
            zkLock.getLock();
            //模拟业务操作
            try {
                System.out.println(Thread.currentThread().getName()+"   正在进行业务操作 ");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            zkLock.releaseLock();
        }

    }
}
