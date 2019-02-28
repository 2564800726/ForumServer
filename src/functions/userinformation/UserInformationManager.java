package functions.userinformation;

import functions.Manager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import utils.constant.Functions;
import utils.constant.Keys;
import utils.json.JsonToHashMap;

import java.util.HashMap;

public class UserInformationManager extends Manager {
    private String function;
    private String[] params;
    private String updateData;

    public UserInformationManager(String function, String[] params) {
        this.function = function;
        this.params = params;
    }

    public UserInformationManager(String function, String updateData) {
        this.updateData = updateData;
        this.function = function;
    }

    @Override
    public String deal() {
        start();
        close();
        return returnData;
    }

    private void start() {
        try {
            switch (function) {
                case Functions.UPDATE_USER_INFORMATION:
                    updateUserInformation();
                    makeReturnData();
                    break;
                case Functions.GET_USER_INFORMATION:
                    getUserInformation();
                    makeReturnData();
                    break;
                case Functions.SUBSCRIBE:
                    getSubscribeUser();
                    break;
                case Functions.FANS:
                    getFans();
                    break;
                case Functions.STAR:
                    getStar();
                    break;
                case Functions.SIGN_IN:
                    signIn();
                    makeReturnData();
                    break;
                default:
                    returnDataHashMap.put(Keys.STATUS, "400");
                    createMessage("请求失败");
                    makeReturnData();
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateUserInformation() {
        JSONObject jsonObject = JSONObject.fromObject(this.updateData).getJSONObject(Keys.UPDATE_DATA);
        HashMap<String, String> updateData = new HashMap<>();
        try {
            for (Object key : jsonObject.keySet()) {
                updateData.put((String) key, jsonObject.getString((String) key));
            }
            if (!updateData.isEmpty()) {
                String account = updateData.get(Keys.ACCOUNT);
                if (databaseConnection.query("user", "password", "account=" + account)
                        .equals(updateData.get(Keys.PASSWORD))) {
                    for (String key : updateData.keySet()) {
                        if (!Keys.ACCOUNT.equals(key) && !Keys.PASSWORD.equals(key)) {
                            if ("userName".equals(key)) {
                                key = "nicName";
                            }
                            databaseConnection.update("user", key, updateData.get(key), "account=" + account);
                        }
                    }
                    returnDataHashMap.put(Keys.STATUS, "200");
                    createMessage("修改成功");
                    return;
                }
            }
            returnDataHashMap.put(Keys.STATUS, "400");
            createMessage("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            returnDataHashMap.put(Keys.STATUS, "200");
            createMessage("修改失败");
        }
    }

    private void getUserInformation() {
        HashMap<String, String> user = databaseConnection.gets("user",
                new String[] {Keys.NIC_NAME, Keys.AGE, Keys.HEAD, Keys.BACKGROUND,
                        Keys.GENDER, Keys.LEVEL, Keys.SUBSCRIBE_COUNT, Keys.FANS_COUNT, Keys.STAR_COUNT,
                        Keys.SIGNATURE, Keys.BIRTHDAY}, params[0]);
        if (user != null) {
            returnDataHashMap.put(Keys.STATUS, "200");
            createMessage("请求成功");
            returnDataHashMap.put(Keys.NIC_NAME, user.get(Keys.NIC_NAME));
            returnDataHashMap.put(Keys.AGE, user.get(Keys.AGE));
            returnDataHashMap.put(Keys.HEAD, user.get(Keys.HEAD));
            returnDataHashMap.put(Keys.BACKGROUND, user.get(Keys.BACKGROUND));
            returnDataHashMap.put(Keys.GENDER, user.get(Keys.GENDER));
            returnDataHashMap.put(Keys.LEVEL, user.get(Keys.LEVEL));
            returnDataHashMap.put(Keys.SUBSCRIBE_COUNT, user.get(Keys.SUBSCRIBE_COUNT));
            returnDataHashMap.put(Keys.FANS_COUNT, user.get(Keys.FANS_COUNT));
            returnDataHashMap.put(Keys.STAR_COUNT, user.get(Keys.STAR_COUNT));
            returnDataHashMap.put(Keys.SIGNATURE, user.get(Keys.SIGNATURE));
            returnDataHashMap.put(Keys.BIRTHDAY, user.get(Keys.BIRTHDAY));
        } else {
            returnDataHashMap.put(Keys.STATUS, "400");
            createMessage("请求失败");
        }
    }

    private void getSubscribeUser() {
        String subscribes = databaseConnection.query("user", "subscribe", params[0]);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (!"".equals(subscribes)) {
            for (String account : subscribes.split(";")) {
                if (!"".equals(account)) {
                    HashMap<String, String> user = databaseConnection.gets("user",
                            new String[]{ Keys.NIC_NAME, Keys.AGE, Keys.HEAD, Keys.GENDER, Keys.LEVEL}, "account=" + account);
                    JSONObject object = new JSONObject();
                    object.put(Keys.NIC_NAME, user.get(Keys.NIC_NAME));
                    object.put(Keys.AGE, user.get(Keys.AGE));
                    object.put(Keys.HEAD, user.get(Keys.HEAD));
                    object.put(Keys.GENDER, user.get(Keys.GENDER));
                    object.put(Keys.LEVEL, user.get(Keys.LEVEL));
                    object.put(Keys.ACCOUNT, account);
                    jsonArray.add(object);
                }
            }
        }
        jsonObject.put(Keys.RETURN_DATA, jsonArray);
        jsonObject.put(Keys.STATUS, "200");
        jsonObject.put(Keys.MESSAGE, "请求成功");
        returnData = jsonObject.toString();
    }

    private void getFans() {
        String subscribes = databaseConnection.query("user", "fans", params[0]);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (!"".equals(subscribes)) {
            for (String account : subscribes.split(";")) {
                if (!"".equals(account)) {
                    HashMap<String, String> user = databaseConnection.gets("user",
                            new String[]{ Keys.NIC_NAME, Keys.AGE, Keys.HEAD, Keys.GENDER, Keys.LEVEL}, "account=" + account);
                    JSONObject object = new JSONObject();
                    object.put(Keys.NIC_NAME, user.get(Keys.NIC_NAME));
                    object.put(Keys.AGE, user.get(Keys.AGE));
                    object.put(Keys.HEAD, user.get(Keys.HEAD));
                    object.put(Keys.GENDER, user.get(Keys.GENDER));
                    object.put(Keys.LEVEL, user.get(Keys.LEVEL));
                    object.put(Keys.ACCOUNT, account);
                    jsonArray.add(object);
                }
            }
        }
        jsonObject.put(Keys.RETURN_DATA, jsonArray);
        jsonObject.put(Keys.STATUS, "200");
        jsonObject.put(Keys.MESSAGE, "请求成功");
        returnData = jsonObject.toString();
    }

    private void getStar() {
        String subscribes = databaseConnection.query("user", "star", params[0]);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (!"".equals(subscribes)) {
            for (String id : subscribes.split(";")) {
                if (!"".equals(id)) {
                    HashMap<String, String> post = databaseConnection.gets("post",
                            new String[]{Keys.POST_TITLE, Keys.POST_AUTHOR, Keys.POST_VISIT, Keys.ICON,
                                    Keys.POST_DISCUSS, Keys.POST_DESCRIPTION, Keys.DATE, Keys.TIME}, "id=" + id);
                    JSONObject object = new JSONObject();
                    object.put(Keys.POST_TITLE, post.get(Keys.POST_TITLE));
                    object.put(Keys.POST_AUTHOR, post.get(Keys.POST_AUTHOR));
                    object.put(Keys.POST_VISIT, post.get(Keys.POST_VISIT));
                    object.put(Keys.ICON, post.get(Keys.ICON));
                    object.put(Keys.POST_DISCUSS, post.get(Keys.POST_DISCUSS));
                    object.put(Keys.POST_DESCRIPTION, post.get(Keys.POST_DESCRIPTION));
                    object.put(Keys.ID, id);
                    object.put(Keys.DATE, post.get(Keys.DATE));
                    object.put(Keys.TIME, post.get(Keys.TIME));
                    jsonArray.add(object);
                }
            }
        }
        jsonObject.put(Keys.RETURN_DATA, jsonArray);
        jsonObject.put(Keys.STATUS, "200");
        jsonObject.put(Keys.MESSAGE, "请求成功");
        returnData = jsonObject.toString();
    }

    private void signIn() {
        HashMap updateData = new JsonToHashMap().getHashMap(this.updateData, new String[]{Keys.ACCOUNT, Keys.PASSWORD, Keys.ID});
        String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
        if (correctPassword != null && correctPassword.equals(updateData.get(Keys.PASSWORD))) {
            if (databaseConnection.isExist("sign", "account", (String) updateData.get(Keys.ACCOUNT), "id=" + updateData.get(Keys.ID))) {
                returnDataHashMap.put(Keys.STATUS, "201");
                createMessage("签到失败");
                return;
            } else {
                databaseConnection.insert("sign", new String[]{(String) updateData.get(Keys.ID), (String) updateData.get(Keys.ACCOUNT)});
                int exp = Integer.parseInt(databaseConnection.query("user", "exp", "account=" + updateData.get(Keys.ACCOUNT)));
                exp += 50;
                databaseConnection.update("user", "exp", String.valueOf(exp), "account=" + updateData.get(Keys.ACCOUNT));
                returnDataHashMap.put(Keys.STATUS, "200");
                createMessage("签到成功");
                return;
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("签到失败");
    }
}
