package server;

import utils.loadconf.Loader;
import utils.database.DatabaseConnectionPool;

public class StartForumServer {
    private void doShutdownWork() {
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatabaseConnectionPool.shutDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("程序结束");
            }
        }));
    }
    public static void main(String[] args) {
        new StartForumServer().doShutdownWork();
        int port;
        try {
            port = Integer.parseInt(Loader.getInstance().getConf("LISTEN_PORT"));
            new ForumServer().startForumServer(port);
        } catch (NumberFormatException numberFormatException) {
            System.out.println();
        }
    }
}
