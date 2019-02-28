package functions.findpassword;

import functions.Manager;
import functions.getverificationcode.GetVerificationCodeManager;
import utils.constant.Keys;
import utils.json.JsonToHashMap;

import java.util.HashMap;

public class FindPasswordManager extends Manager {
    public FindPasswordManager(String returnData) {
        super();
        this.returnData = returnData;
    }

    @Override
    public String deal() {
        HashMap<String, String> returnData1 = new JsonToHashMap().getHashMap(returnData,
                new String[]{Keys.ACCOUNT, Keys.PASSWORD, Keys.VERIFICATION_CODE});
        if (GetVerificationCodeManager.verifyVerificationCode(
                returnData1.get(Keys.ACCOUNT), returnData1.get(Keys.VERIFICATION_CODE))) {
            start(returnData1.get(Keys.ACCOUNT), returnData1.get(Keys.PASSWORD));
        } else {
            createMessage("验证码错误");
            returnDataHashMap.put(Keys.STATUS, "400");
        }
        close();
        makeReturnData();
        return returnData;
    }

    private void start(String account, String password) {
        if (databaseConnection.update("user", "password", password, "account=" + account)) {
            createMessage("找回成功");
            returnDataHashMap.put(Keys.ACCOUNT, account);
            returnDataHashMap.put(Keys.PASSWORD, password);
            returnDataHashMap.put(Keys.STATUS, "200");
        } else {
            createMessage("找回失败");
            returnDataHashMap.put(Keys.STATUS, "400");
        }
    }
}
