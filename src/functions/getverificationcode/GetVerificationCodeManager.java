package functions.getverificationcode;

import functions.Manager;
import utils.constant.Keys;
import utils.database.DatabaseConnectionPool;
import utils.database.DatabaseManager;
import utils.json.JsonToHashMap;

import java.util.HashMap;
import java.util.Random;

public class GetVerificationCodeManager extends Manager {
    private String verificationCode;
    private String account;

    public GetVerificationCodeManager(String updateData) {
        HashMap<String, String> updateData1 = new JsonToHashMap().getHashMap(updateData, new String[]{Keys.ACCOUNT});
        this.account = updateData1.get(Keys.ACCOUNT);
    }

    @Override
    public String deal() {
        start();
        close();
        makeReturnData();
        return returnData;
    }

    private void start() {
        createVerificationCode();
        if (sendVerificationCode()) {
            createMessage("验证码已发送");
            returnDataHashMap.put(Keys.STATUS, "200");
        } else {
            createMessage("获取验证码失败");
            returnDataHashMap.put(Keys.STATUS, "400");
        }
    }

    private boolean sendVerificationCode() {
        //
        return true;
    }

    private void createVerificationCode() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            stringBuilder.append(Math.abs(random.nextInt() % 10));
        }
        verificationCode = stringBuilder.toString();
        System.out.println(verificationCode + " 验证码");
        if (databaseConnection.isExist("verification_code", "account", account)) {
            databaseConnection.delete("verification_code", "account=" + account);
        } else {
            databaseConnection.insert("verification_code", new String[]{account, verificationCode});
        }
        new Thread(() -> {
            try {
                Thread.sleep(30000 * 2 * 30);
                databaseConnection.delete(
                        "verification_code", "verification_code=" + verificationCode);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static boolean verifyVerificationCode(String account, String verificationCode) {
        String correctVerificationCode;
        DatabaseManager databaseConnection = DatabaseConnectionPool.getConnection();
        if (databaseConnection.isExist("verification_code", "account", account)) {
            correctVerificationCode = databaseConnection.query(
                    "verification_code", "verification_code", "account=" + account);
            if (correctVerificationCode.equals(verificationCode)) {
                databaseConnection.delete("verification_code", "verification_code=" + correctVerificationCode);
                DatabaseConnectionPool.close(databaseConnection);
                return true;
            }
        }
        DatabaseConnectionPool.close(databaseConnection);
        return false;
    }
}
