package server;

import functions.getverificationcode.GetVerificationCodeManager;
import functions.login.LoginManager;
import functions.plates.PlatesManager;
import functions.posts.PostsManager;
import functions.subscribe.SubscribeManager;
import functions.userinformation.UserInformationManager;
import utils.loadconf.Loader;
import utils.constant.Functions;
import functions.register.RegisterManager;

import java.io.*;
import java.net.Socket;

public class HttpServer extends Thread {
    private String listenPort;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private String returnData = null;
    private String updateData = null;

    @Override
    public void run() {
        readHttp();
    }

    /**
     * 获取socket的输入输出流
     * @param socket  客户端的请求
     */
    public HttpServer(Socket socket) {
        loadConf();
        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析浏览器的请求
     */
    private void readHttp() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String firstLine = bufferedReader.readLine();
            if (firstLine != null) {
                switch (firstLine.split(" ")[0]) {
                    case "GET":
                        System.out.println(firstLine);
                        get(firstLine.split(" ")[1].split("/")[1]);
                        break;
                    case "POST":
                        System.out.println(firstLine);
                        post(bufferedReader, firstLine.split(" ")[1].split("/")[1]);
                        break;
                    case "PUT":
                        System.out.println(firstLine);
                        put(bufferedReader, firstLine.split(" ")[1].split("/")[1]);
                        break;
                    case "DELETE":
                        System.out.println(firstLine);
                        delete(firstLine.split(" ")[1].split("/")[1]);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Delete方法
     * @param url  url
     */
    private void delete(String url) {

    }

    /**
     * Put方法
     * @param bufferedReader  输入流
     * @param url  url
     */
    private void put(BufferedReader bufferedReader, String url) {

    }

    /**
     * Post请求
     * @param bufferedReader  输入流
     * @param url  url
     */
    private void post(BufferedReader bufferedReader, String url) {
        System.out.println(url);
        String function = url.split("\\?")[0];
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            try {
                socket.setSoTimeout(3000);
                boolean start = false;
                while ((line = bufferedReader.readLine()) != null) {
                    if ("".equals(line) && !start) {
                        start = true;
                        continue;
                    }
                    if (start && !"".equals(line)) {
                        stringBuilder.append(line);
                        continue;
                    }
                    if ("".equals(line)) {
                        break;
                    }
                }
            } catch (Exception e) {
                socket.shutdownInput();
            }
            updateData = stringBuilder.toString();
            switch (url) {
                case Functions.REGISTER:
                    returnData = new RegisterManager(updateData).deal();
                    break;
                case Functions.LOGIN:
                    returnData = new LoginManager(updateData).deal();
                    break;
                case Functions.FIND_PASSWORD:

                    break;
                case Functions.COMMENT:
                case Functions.STAR_POST:
                case Functions.PRAISE_POST:
                case Functions.CHECK_PRAISE:
                case Functions.CHECK_STAR:
                case Functions.GET_POST_DETAIL:
                    returnData = new PostsManager(function, updateData).deal();
                    break;
                case Functions.GET_VERIFICATION_CODE:
                    System.out.println("开始获取");
                    returnData = new GetVerificationCodeManager(updateData).deal();
                    break;
                case Functions.SUBSCRIBE:
                case Functions.CHECK_SUBSCRIBE:
                    returnData = new SubscribeManager(function, updateData).deal();
                    break;
                case Functions.UPDATE_USER_INFORMATION:
                case Functions.SIGN_IN:
                    returnData = new UserInformationManager(function, updateData).deal();
                    break;
                case Functions.UPLOAD_IMAGE:
                default:
                    returnData = "{\"\": ,\"returnData\": {\"message\": \"ERROR\"}}" + "\r\n";
                    break;
            }
            setHead("ok", 200);
            assert returnData != null;
            String stringBuilder1 = String.valueOf(returnData.length()) + "\r\n\r\n" +
                    returnData + "\r\n";
            outputStream.write(stringBuilder1.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            setHead("no", 400);
            returnData = "{\"\": ,\"returnData\": {\"message\": \"ERROR\"}}" + "\r\n";
            String stringBuilder1 = String.valueOf(returnData.length()) + "\r\n\r\n" +
                    returnData + "\r\n";
            try {
                outputStream.write(stringBuilder1.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Get请求
     * @param url  url
     */
    private void get(String url) {
        System.out.println(url);
        String[] params = null;
        String function = url.split("\\?")[0];
        try {
            if (url.contains("?")) {
                params = url.split("\\?")[1].split("&");
            }
            switch (function) {
                case Functions.SEARCH:
                case Functions.GET_POST_TOTAL_PAGES:
                case Functions.RECOMMEND:
                case Functions.POSTS:
                case Functions.GET_POST_DETAIL:
                    returnData = new PostsManager(function, params).deal();
                    break;
                case Functions.GET_PLATES:
                case Functions.GET_PLATE_INFORMATION:
                    returnData = new PlatesManager(function, params).deal();
                    break;
                case Functions.SUBSCRIBE:
                    if (params != null && params[0].contains("subscribe")) {
                        returnData = new SubscribeManager(function, params).deal();
                    } else {
                        returnData = new UserInformationManager(function, params).deal();
                    }
                    break;
                case Functions.GET_USER_INFORMATION:
                case Functions.FANS:
                case Functions.STAR:
                    returnData = new UserInformationManager(function, params).deal();
                    break;
                default:
                returnData = "{\"\": ,\"returnData\": {\"message\": \"ERROR\"}}" + "\r\n";
                break;
            }
            setHead("ok", 200);
            assert returnData != null;
            String stringBuilder1 = String.valueOf(returnData.length()) + "\r\n\r\n" +
                    returnData + "\r\n";
            outputStream.write(stringBuilder1.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            setHead("no", 400);
            returnData = "{\"\": ,\"returnData\": {\"message\": \"ERROR\"}}" + "\r\n";
            String stringBuilder1 = String.valueOf(returnData.length()) + "\r\n\r\n" +
                    returnData + "\r\n";
            try {
                outputStream.write(stringBuilder1.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 设置响应报文头部
     * @param result  请求的结果
     * @param resultCode  返回码
     */
    private void setHead(String result, int resultCode) {
        StringBuilder correctResult = new StringBuilder();
        correctResult.append("HTTP/1.1 ").append(resultCode).append(" ").append(result).append("\r\n");
        correctResult.append("Connect-Type: text/html\r\n");
        correctResult.append("Connect-Length: ");
        try {
            outputStream.write(correctResult.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载配置文件
     */
    private void loadConf() {
        listenPort = Loader.getInstance().getConf("LISTEN_PORT");
    }
}
