package functions.subscribe;

import functions.Manager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import utils.constant.Functions;
import utils.constant.Keys;
import utils.json.JsonToHashMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SubscribeManager extends Manager {
    private String function;
    private String[] params;

    public SubscribeManager(String function, String updateData) {
        this.function = function;
        this.updateData = updateData;
    }

    public SubscribeManager(String function, String[] params) {
        this.function = function;
        this.params = params;
    }

    @Override
    public String deal() {
        try {
            switch (function) {
                case Functions.CHECK_SUBSCRIBE:
                    checkSubscribe();
                    makeReturnData();
                    break;
                case Functions.SUBSCRIBE:
                    if (params == null) {
                        subscribe();
                        makeReturnData();
                    } else {
                        getSubscribePosts();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
        return returnData;
    }

    // 订阅用户或者板块
    private void subscribe() {
        HashMap<String, String> updateData = new JsonToHashMap().getHashMap(this.updateData, new String[]{Keys.ACCOUNT, Keys.USER, Keys.PASSWORD});
        if (updateData != null) {
            String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
            if (correctPassword != null) {
                if (correctPassword.equals(updateData.get(Keys.PASSWORD))) {
                    String subscribe = databaseConnection.query("user", "subscribe", "account=" + updateData.get(Keys.ACCOUNT));
                    String fan = databaseConnection.query("user", "fans", "account=" + updateData.get(Keys.USER));
                    int subscribeCount;
                    int fansCount;
                    subscribeCount = subscribe.split(";").length - 1;
                    fansCount = fan.split(";").length - 1;
                    if (!subscribe.contains(";" + updateData.get(Keys.USER) + ";")) {
                        subscribe += updateData.get(Keys.USER) + ";";
                        fan += updateData.get(Keys.ACCOUNT) + ";";
                        subscribeCount++;
                        fansCount++;
                        returnDataHashMap.put(Keys.STATUS, "200");
                        createMessage("关注成功");
                    } else {
                        ArrayList<String> subscribes = new ArrayList<>(Arrays.asList(subscribe.split(";")));
                        ArrayList<String> fans = new ArrayList<>(Arrays.asList(fan.split(";")));
                        subscribes.remove(updateData.get(Keys.USER));
                        fans.remove(updateData.get(Keys.USER));
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String account : subscribes) {
                            stringBuilder.append(account).append(";");
                        }
                        subscribe = stringBuilder.toString();
                        for (String account : fans) {
                            stringBuilder.append(account).append(";");
                        }
                        fan = stringBuilder.toString();
                        subscribeCount--;
                        fansCount--;
                        returnDataHashMap.put(Keys.STATUS, "201");
                        createMessage("取消关注");
                    }
                    databaseConnection.update("user", "subscribe", subscribe, "account=" + updateData.get(Keys.ACCOUNT));
                    databaseConnection.update("user", "fans", fan, "account=" + updateData.get(Keys.USER));
                    databaseConnection.update("user", "subscribeCount", String.valueOf(subscribeCount), "account=" + updateData.get(Keys.ACCOUNT));
                    databaseConnection.update("user", "fansCount", String.valueOf(fansCount), "account=" + updateData.get(Keys.USER));
                    return;
                }
            }
        }else {
            updateData = new JsonToHashMap().getHashMap(this.updateData, new String[]{Keys.ACCOUNT, Keys.PASSWORD, Keys.ID});
            String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
            if (correctPassword != null) {
                if (correctPassword.equals(updateData.get(Keys.PASSWORD))) {
                    String subscribePlate = databaseConnection.query("user", "subscribe_plate", "account=" + updateData.get(Keys.ACCOUNT));
                    if (!subscribePlate.contains(";" + updateData.get(Keys.ID) + ";")) {
                        subscribePlate += updateData.get(Keys.ID) + ";";
                    } else {
                        ArrayList<String> subscribePlates = new ArrayList<>(Arrays.asList(subscribePlate.split(";")));
                        subscribePlates.remove(updateData.get(Keys.ID));
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String id : subscribePlates) {
                            if (!"".equals(id)) {
                                stringBuilder.append(id).append(";");
                            }
                        }
                        subscribePlate = stringBuilder.toString();
                    }
                    databaseConnection.update("user", "subscribe_plate", subscribePlate, "account=" + updateData.get(Keys.ACCOUNT));
                }
            }
            returnDataHashMap.put(Keys.STATUS, "200");
            createMessage("订阅成功");
            return;
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("请求失败");
    }

    // 检查是否已经关注该用户
    private void checkSubscribe() {
        HashMap updateData = new JsonToHashMap().getHashMap(this.updateData, new String[]{Keys.ACCOUNT, Keys.PASSWORD, Keys.USER});
        String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
        if (correctPassword != null) {
            if (correctPassword.equals(updateData.get(Keys.PASSWORD))) {
                String subscribe = databaseConnection.query("user", "subscribe", "account=" + updateData.get(Keys.ACCOUNT));
                if (subscribe.contains(";" + updateData.get(Keys.USER) + ";")) {
                    returnDataHashMap.put(Keys.STATUS, "200");
                    createMessage("已关注");
                    return;
                }
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("未关注");
    }

    // 获取订阅的用户发表的帖子
    private void getSubscribePosts() throws SQLException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        String subscribe = databaseConnection.query("user", "subscribe", "account=" + params[0].split("=")[1]);
        if (subscribe != null && !";".equals(subscribe)) {
            String[] subscribes = subscribe.split(";");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT * FROM post WHERE ");
            int start = 0;
            int end = 0;
            for (String account : subscribes) {
                if (!"".equals(account)) {
                    stringBuilder.append("author=").append(account);
                    start = stringBuilder.length();
                    stringBuilder.append(" OR ");
                    end = stringBuilder.length();
                }
            }
            stringBuilder.delete(start, end);
            stringBuilder.replace(start, end, " ");
            stringBuilder.append("ORDER BY edit_date DESC, edit_time DESC;");
            System.out.println(stringBuilder.toString());
            ResultSet resultSet = databaseConnection.getResultSet(stringBuilder.toString());
            while (resultSet.next()) {
                JSONObject object =  new JSONObject();
                object.put(Keys.POST_TITLE, resultSet.getString("title"));
                object.put(Keys.POST_DESCRIPTION, resultSet.getString("description"));
                object.put(Keys.POST_DISCUSS, resultSet.getString("discuss"));
                object.put(Keys.POST_VISIT, resultSet.getString("visit"));
                object.put(Keys.POST_AUTHOR, resultSet.getString("author"));
                object.put(Keys.ICON, resultSet.getString("icon"));
                object.put(Keys.ID, resultSet.getString("id"));
                object.put(Keys.DATE, resultSet.getString("edit_date"));
                object.put(Keys.TIME, resultSet.getString("edit_time"));
                jsonArray.add(object);
            }
        }
        jsonObject.put(Keys.RETURN_DATA, jsonArray);
        jsonObject.put(Keys.STATUS, "200");
        jsonObject.put(Keys.MESSAGE, "请求成功");
        returnData = jsonObject.toString();
    }
}
