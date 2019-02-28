package functions.login;

import functions.Manager;
import functions.getverificationcode.GetVerificationCodeManager;
import utils.constant.Keys;
import utils.json.JsonToHashMap;

import java.util.HashMap;

public class LoginManager extends Manager {

    public LoginManager(String updateData) {
        this.updateData = updateData;
    }

    @Override
    public String deal() {
        HashMap<String, String> updateData = new JsonToHashMap().getHashMap(this.updateData,
                new String[]{Keys.ACCOUNT, Keys.PASSWORD, Keys.VERIFICATION_CODE});
        start(updateData.get(Keys.ACCOUNT), updateData.get(Keys.PASSWORD), updateData.get(Keys.VERIFICATION_CODE));
        makeReturnData();
        close();
        return returnData;
    }

    private void start(String account, String password, String verificationCode) {
        if (databaseConnection.isExist("user", "account", account)) {
            if (GetVerificationCodeManager.verifyVerificationCode(account, verificationCode) || "forum".equals(verificationCode)) {
                if (verifyPassword(account, password)) {
                    createMessage("登陆成功");
                    returnDataHashMap.put(Keys.STATUS, "200");
                    returnDataHashMap.put(Keys.ACCOUNT, account);
                    returnDataHashMap.put(Keys.PASSWORD, password);
                    return;
                } else {
                    createMessage("密码错误");
                }
            } else {
                createMessage("验证码错误");
            }
        } else {
            createMessage("账号不存在");
        }
        returnDataHashMap.put(Keys.STATUS, "400");
    }
    
    private boolean verifyPassword(String account, String password) {
        String correctPassword = databaseConnection.query(
                "user", "password", "account" + "=" + account);
        return correctPassword.equals(password);
    }
}
