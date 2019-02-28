package utils.json;

import net.sf.json.JSONObject;
import utils.constant.Keys;

import java.time.LocalDateTime;
import java.util.HashMap;

public class HashMapToJson {
    /**
     * 将需要上传服务器的数据组装成为Json字符串
     * @param data  需要上传的数据
     * @return  组装之后的Json字符串
     */
    public String getJson(HashMap<String, String> data) {
        JSONObject jsonObject = new JSONObject();
        JSONObject returnData = new JSONObject();
        try {
            for (String key : data.keySet()) {
                if (Keys.MESSAGE.equals(key)) {
                    jsonObject.put(key, data.get(key));
                } else if (Keys.STATUS.equals(key)) {
                    jsonObject.put(key, data.get(key));
                } else {
                    returnData.put(key, data.get(key));
                }
            }
            jsonObject.put(Keys.RETURN_DATA, returnData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
