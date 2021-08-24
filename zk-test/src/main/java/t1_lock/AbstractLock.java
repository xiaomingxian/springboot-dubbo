package t1_lock;


import org.I0Itec.zkclient.ZkClient;
public abstract class AbstractLock {

    //zk地址和端口
    public static final String ZK_ADDR = "49.234.25.12:2181";
    //超时时间
    public static final int SESSION_TIMEOUT = 10000;
    //创建zk
    protected ZkClient zkClient = new ZkClient(ZK_ADDR, SESSION_TIMEOUT);


    /**
     * 可以认为是模板模式，两个子类分别实现它的抽象方法
     * 1，简单的分布式锁
     * 2，高性能分布式锁
     */
    public void getLock() {
        String threadName = Thread.currentThread().getName();
        if (tryLock()) {
            System.out.println(threadName+"-获取锁成功");
        }else {
            System.out.println(threadName+"-获取锁失败，进行等待...");
            //simple锁   订阅节点的变化 存在则阻塞 不存在就 删除节点监听  再进行下一步 递归开始锁竞争
            //有序临时锁  监听前一个节点的变化 前一个节点在 就阻塞 前一个节点被删除 就删除前一个节点监听 可进行下一步 递归开始竞争锁
            //因为是有序节点 且每次都在前一个节点删除后才 下一个节点才会释放锁 所以实现了公平锁
            waitLock();
            //递归重新获取锁
            getLock();
        }
    }

    public abstract void releaseLock();

    public abstract boolean tryLock();

    public abstract void waitLock();
}