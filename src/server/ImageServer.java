package server;

import utils.database.DatabaseConnectionPool;
import utils.database.DatabaseManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ImageServer extends Thread {
    private InputStream inputStream = null;
    private Socket socket = null;
    private FileOutputStream fileOutputStream = null;

    public ImageServer(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
    }

    @Override
    public void run() {
        try {
            // 读取头部
            byte[] accountByte = new byte[11];
            inputStream.read(accountByte);
            byte[] passwordByte = new byte[18];
            inputStream.read(passwordByte);
            byte[] keyByte = new byte[3];
            inputStream.read(keyByte);
            byte[] md5Byte = new byte[32];
            inputStream.read(md5Byte);

            String account = new String(accountByte);
            String password = new String(passwordByte);
            String key = new String(keyByte);
            String md5 = new String(md5Byte);

            DatabaseManager connection = DatabaseConnectionPool.getConnection();
            String correctPassword = connection.query("user", "password", "account=" + account);
            if (password.equals(correctPassword)) {
                fileOutputStream = new FileOutputStream(new File("/var/www/html/" + md5 + ".png"));
                byte[] buffer = new byte[1024];
                int length = -1;
                while ((length = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.flush();
                connection.update("user", "head", md5 + ".png", "account=" + account);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
