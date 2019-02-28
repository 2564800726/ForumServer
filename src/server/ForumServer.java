package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ForumServer {
    public void startForumServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                new HttpServer(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
