package functions.register;

import functions.Manager;
import functions.getverificationcode.GetVerificationCodeManager;
import utils.constant.Keys;
import utils.json.JsonToHashMap;

import java.util.HashMap;

public class RegisterManager extends Manager {

    public RegisterManager(String updateData) {
        this.updateData = updateData;
    }

    @Override
    public String deal() {
        HashMap<String, String> Message = new JsonToHashMap().getHashMap(this.updateData,
                new String[]{Keys.ACCOUNT, Keys.PASSWORD, Keys.VERIFICATION_CODE});
        if (GetVerificationCodeManager.verifyVerificationCode(
                Message.get(Keys.ACCOUNT), Message.get(Keys.VERIFICATION_CODE))) {
            start(Message.get(Keys.ACCOUNT), Message.get(Keys.PASSWORD));
        } else {
            createMessage("验证码错误");
            returnDataHashMap.put(Keys.STATUS, "400");
        }
        close();
        makeReturnData();
        return returnData;
    }

    private void start(String account, String password) {
        if (!databaseConnection.isExist("user", "account", account)) {
            if (databaseConnection.insert("user",
                    new String[]{"account", "password", "subscribe", "fans", "subscribe_plate", "star", "response",
                            "nicName", "signature", "gender", "head", "background"},
                    new String[]{account, password, ";", ";", ";1;2;3;4;5;", ";", "", account, "什么都没有~", "male",
                            "http://129.204.3.245/default_head.png", ""})) {
                createMessage("注册成功");
                returnDataHashMap.put(Keys.ACCOUNT, account);
                returnDataHashMap.put(Keys.PASSWORD, password);
                returnDataHashMap.put(Keys.STATUS, "200");
                return;
            } else {
                createMessage("注册失败");
            }
        } else {
            createMessage("该账号已经注册");
        }
        returnDataHashMap.put(Keys.STATUS, "400");
    }
}
