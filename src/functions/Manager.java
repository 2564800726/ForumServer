package functions;

import utils.constant.Keys;
import utils.database.DatabaseConnectionPool;
import utils.database.DatabaseManager;
import utils.json.HashMapToJson;

import java.util.HashMap;

public abstract class Manager {
    protected String updateData;
    protected String returnData;
    protected HashMap<String, String> returnDataHashMap = new HashMap<>();
    protected DatabaseManager databaseConnection;

    protected Manager() {
        databaseConnection = DatabaseConnectionPool.getConnection();
    }

    /**
     * 对客户端上传的数据进行处理并返回返回给客户端的数据
     * @return  需要返回给客户端的数据
     */
    public abstract String deal();

    protected void createMessage(String message) {
        returnDataHashMap.put(Keys.MESSAGE, message);
    }

    protected void makeReturnData() {
        this.returnData = new HashMapToJson().getJson(returnDataHashMap);
    }

    protected void close() {
        DatabaseConnectionPool.close(databaseConnection);
    }
}
