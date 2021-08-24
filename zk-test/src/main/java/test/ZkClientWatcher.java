package test;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class ZkClientWatcher implements Watcher {
    // 集群连接地址
    private static final String CONNECT_ADDRES = "49.234.25.12:2181";
    // 会话超时时间
    private static final int SESSIONTIME = 2000;
    // 信号量,让zk在连接之前等待,连接成功后才能往下走.
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    private static String LOG_MAIN = "【main】 ";
    private ZooKeeper zk;

    public void createConnection(String connectAddres, int sessionTimeOut) {
        try {
            zk = new ZooKeeper(connectAddres, sessionTimeOut, this);
            System.out.println(LOG_MAIN + "zk 开始启动连接服务器....");
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean createPath(String path, String data) {
        try {
            this.exists(path, true); // 允许事件监听
            this.zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println(LOG_MAIN + "节点创建成功, Path:" + path + ",data:" + data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断指定节点是否存在
     *
     * @param path
     *            节点路径
     */
    public Stat exists(String path, boolean needWatch) {
        try {
            return this.zk.exists(path, needWatch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateNode(String path,String data) throws KeeperException, InterruptedException {
        exists(path, true);
        this.zk.setData(path, data.getBytes(), -1);
        return false;
    }
    // zk节点、发生变更、删除、修改 、 新增 事件通知
    @Override
    public void process(WatchedEvent watchedEvent) {

        // 获取事件状态
        Event.KeeperState keeperState = watchedEvent.getState();
        // 获取事件类型
        Event.EventType eventType = watchedEvent.getType();
        // zk 路径
        String path = watchedEvent.getPath();
        System.out.println("进入到 process() keeperState:" + keeperState + ", eventType:" + eventType + ", path:" + path);
        // 判断是否建立连接
        if (Event.KeeperState.SyncConnected == keeperState) {
            if (Event.EventType.None == eventType) {
                // 如果建立建立成功,让后程序往下走
                System.out.println(LOG_MAIN + "zk 建立连接成功!");
                countDownLatch.countDown();
            } else if (Event.EventType.NodeCreated == eventType) {
                System.out.println(LOG_MAIN + "事件通知,新增node节点" + path);
            } else if (Event.EventType.NodeDataChanged == eventType) {
                System.out.println(LOG_MAIN + "事件通知,当前node节点" + path + "被修改....");
            }
            else if (Event.EventType.NodeDeleted == eventType) {
                System.out.println(LOG_MAIN + "事件通知,当前node节点" + path + "被删除....");
            }

        }
        System.out.println("--------------------------------------------------------");
    }

    public static void main(String[] args) throws KeeperException, InterruptedException {
        ZkClientWatcher zkClientWatcher = new ZkClientWatcher();
        zkClientWatcher.createConnection(CONNECT_ADDRES, SESSIONTIME);
//        boolean createResult = zkClientWatcher.createPath("/p15", "pa-644064");
        zkClientWatcher.updateNode("/p15","7894561");
    }

}