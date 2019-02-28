package functions.plates;

import functions.Manager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import utils.constant.Functions;
import utils.constant.Keys;
import utils.json.JsonToHashMap;

import java.util.ArrayList;
import java.util.HashMap;

public class PlatesManager extends Manager {
    private String[] params;
    private String function;

    public PlatesManager(String function, String[] params) {
        this.function = function;
        this.params = params;
    }

    public PlatesManager(String updateData) {
        this.updateData = updateData;
    }

    @Override
    public String deal() {
        start();
        close();
        return returnData;
    }

    private void start() {
        switch (function) {
            case Functions.GET_PLATES:
                getPlatesList();
                break;
            case Functions.GET_PLATE_INFORMATION:
                getPlateInformation();
                makeReturnData();
                break;
        }
    }

    /**
     * 获取板块列表
     */
    private void getPlatesList() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (params == null) {
            try {
                jsonObject.put(Keys.STATUS, "200");
                jsonObject.put(Keys.MESSAGE, "请求成功");
                ArrayList<HashMap<String, String>> result = databaseConnection.gets("plate", new String[]{"id", "name", "icon"});
                if (result != null) {
                    for (HashMap<String, String> plate : result) {
                        JSONObject object = new JSONObject();
                        object.put(Keys.ID, plate.get(Keys.ID));
                        object.put(Keys.PLATE_NAME, plate.get(Keys.PLATE_NAME));
                        object.put(Keys.ICON, plate.get(Keys.ICON));
                        jsonArray.add(object);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                jsonObject.put(Keys.STATUS, "400");
                jsonObject.put(Keys.MESSAGE, "请求失败");
            }
        } else {
            String subscribe = databaseConnection.query("user", "subscribe_plate", params[0] + " ORDER BY id ASC");
            if (subscribe != null) {
                String[] result = subscribe.split(";");
                try {
                    jsonObject.put(Keys.STATUS, "200");
                    jsonObject.put(Keys.MESSAGE, "请求成功");
                    for (String plateId : result) {
                        if (!"".equals(plateId)) {
                            HashMap<String, String> plate = databaseConnection.gets("plate", new String[] {"id", "name", "icon"}, "id=" + plateId);
                            JSONObject object = new JSONObject();
                            for (String key : plate.keySet()) {
                                object.put(key, plate.get(key));
                            }
                            jsonArray.add(object);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    jsonObject.put(Keys.STATUS, "400");
                    jsonObject.put(Keys.MESSAGE, "请求失败");
                }
            }
        }
        jsonObject.put(Keys.RETURN_DATA, jsonArray);
        returnData = jsonObject.toString();
    }

    /**
     * 获取板块信息
     */
    private void getPlateInformation() {
        returnDataHashMap.put(Keys.STATUS, "200");
        createMessage("请求成功");
        returnDataHashMap.put(Keys.ID, databaseConnection.query("plate", "id", params[0]));
        returnDataHashMap.put(Keys.ICON, databaseConnection.query("plate", "icon", params[0]));
        returnDataHashMap.put(Keys.PLATE_NAME, databaseConnection.query("plate", "name", params[0]));
    }
}
