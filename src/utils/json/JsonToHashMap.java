package utils.json;

import net.sf.json.JSONObject;
import utils.constant.Keys;

import java.util.HashMap;

public class JsonToHashMap {
    public HashMap<String, String> getHashMap(String returnData, String[] keys) {
        System.out.println(returnData);
        HashMap<String, String> finalData = new HashMap<>();
        try {
            JSONObject jsonObject = JSONObject.fromObject(returnData).getJSONObject(Keys.UPDATE_DATA);
            if (jsonObject != null) {
                for (String key : keys) {
                    finalData.put(key, jsonObject.getString(key));
                }
            }
            return finalData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
