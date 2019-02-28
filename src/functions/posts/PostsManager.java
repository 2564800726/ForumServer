package functions.posts;

import functions.Manager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import utils.constant.Functions;
import utils.constant.Keys;
import utils.database.DatabaseConnectionPool;
import utils.database.DatabaseManager;
import utils.json.JsonToHashMap;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PostsManager extends Manager {
    private String[] params;
    private String function;

    public PostsManager(String function, String[] params) {
        this.function = function;
        this.params = params;
    }

    public PostsManager(String function, String updateData) {
        this.function = function;
        this.updateData = updateData;
    }

    @Override
    public String deal() {
        try {
            switch (function) {
                case Functions.RECOMMEND:
                    if (params == null) {
                        recommend();
                    } else {
                        top();
                        break;
                    }
                    makeReturnData();
                    break;
                case Functions.POSTS:
                    post();
                    break;
                case Functions.GET_POST_DETAIL:
                    if (params != null) {
                        detail();
                    } else {
                        postPost();
                        makeReturnData();
                    }
                    break;
                case Functions.STAR_POST:
                    starPost();
                    makeReturnData();
                    break;
                case Functions.PRAISE_POST:
                    praisePost();
                    makeReturnData();
                    break;
                case Functions.CHECK_STAR:
                    checkStar();
                    makeReturnData();
                    break;
                case Functions.CHECK_PRAISE:
                    checkPraise();
                    makeReturnData();
                    break;
                case Functions.COMMENT:
                    commentPost();
                    makeReturnData();
                    break;
                case Functions.GET_POST_TOTAL_PAGES:
                    getPostTotalPages();
                    makeReturnData();
                    break;
                case Functions.SEARCH:
                    searchPost();
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
        return returnData;
    }

    /**
     * 置顶推荐（包含详细的信息）
     */
    private void recommend() {
        if (params == null) {
            String constraint = "id=" + databaseConnection.query("recommend", "id", "plate='NULL'");
            returnDataHashMap.put(Keys.POST_TITLE, databaseConnection.query("post", "title", constraint));
            returnDataHashMap.put(Keys.POST_DESCRIPTION, databaseConnection.query("post", "description", constraint));
            returnDataHashMap.put(Keys.POST_DISCUSS, databaseConnection.query("post", "discuss", constraint));
            returnDataHashMap.put(Keys.POST_VISIT, databaseConnection.query("post", "visit", constraint));
            returnDataHashMap.put(Keys.POST_AUTHOR, databaseConnection.query("post", "author", constraint));
            returnDataHashMap.put(Keys.ICON, databaseConnection.query("post", "icon", constraint));
            returnDataHashMap.put(Keys.ID, databaseConnection.query("post", "id", constraint));
            returnDataHashMap.put(Keys.DATE, databaseConnection.query("post", "edit_date", constraint));
            returnDataHashMap.put(Keys.TIME, databaseConnection.query("post", "edit_time", constraint));
            returnDataHashMap.put(Keys.STATUS, "200");
            createMessage("请求成功");
        }
    }

    /**
     * 置顶推荐（仅包含标题）
     */
    private void top() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (params != null) {
            List<String> ids = databaseConnection.queryAll("recommend", "id", "plate=" + params[0].split("=")[1]);
            for (String id : ids) {
                String constraint = "id=" + id;
                JSONObject object = new JSONObject();
                object.put(Keys.POST_TITLE, databaseConnection.query("post", "title", constraint));
                object.put(Keys.ID, id);
                jsonArray.add(object);
            }
            jsonObject.put(Keys.STATUS, "200");
            jsonObject.put(Keys.MESSAGE, "请求成功");
        }
        jsonObject.put(Keys.RETURN_DATA, jsonArray);
        jsonObject.put(Keys.MESSAGE, "请求失败");
        returnData = jsonObject.toString();
    }

    /**
     * 普通的帖子
     */
    private void post() {
        if (params != null) {
            String sql = null;
            if (params[0].contains("account")) {
                sql = "SELECT * FROM post WHERE " + params[0].replace("account", "author") + " ORDER BY edit_date DESC, edit_time DESC";
            } else {
                sql = "SELECT * FROM post WHERE " + params[0] + " ORDER BY edit_date DESC, edit_time DESC";
            }
            ResultSet resultSet = databaseConnection.getResultSet(sql);
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            try {
                while (resultSet.next()) {
                    JSONObject object = new JSONObject();
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
                jsonObject.put(Keys.STATUS, "200");
                jsonObject.put(Keys.MESSAGE, "情求成功");
                jsonObject.put(Keys.RETURN_DATA, jsonArray);
                returnData = jsonObject.toString();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        createMessage("请求失败");
        returnDataHashMap.put(Keys.STATUS, "400");
    }

    /**
     * 获取帖子的详情
     */
    private void detail() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        ResultSet resultSet = null;
        if (!"".equals(params[0])) {
            try {
                int index = Integer.parseInt(params[1].split("=")[1]);
                if (index == 0) {
                    String sql = "SELECT * FROM floor WHERE " + params[0] + " AND floor BETWEEN 0 AND 20 ORDER BY date,time ASC";
                    resultSet = databaseConnection.getResultSet(sql);
                } else if (index > 0) {
                    String sql = "SELECT * FROM floor WHERE " + params[0] + " AND floor BETWEEN " + (20 * index + 1) + " AND " + (20 * index + 20);
                    resultSet = databaseConnection.getResultSet(sql);
                } else {
                    jsonObject.put(Keys.MESSAGE, "参数错误");
                    jsonObject.put(Keys.STATUS, "400");
                }
                if (resultSet != null) {
                    List<HashMap<String, String>> floors = new ArrayList<>();
                    while (resultSet.next()) {
                        HashMap<String, String> floor = new HashMap<>();
                        floor.put("account", resultSet.getString("account"));
                        floor.put("pid", resultSet.getString("pid"));
                        floor.put("content", resultSet.getString("content"));
                        floor.put("floor", resultSet.getString("floor"));
                        floor.put("date", resultSet.getString("date"));
                        floors.add(floor);
                    }
                    resultSet.close();
                    String author = floors.get(0).get("account");
                    for (HashMap<String, String> floor : floors) {
                        String account = floor.get("account");
                        String pid = floor.get("pid");
                        String content = floor.get("content");
                        String floor1 = floor.get("floor");
                        String date = floor.get("date");
                        JSONObject object = new JSONObject();
                        object.put(Keys.HEAD, databaseConnection.query("user", "head", "account=" + account));
                        object.put(Keys.USER_NAME, databaseConnection.query("user", "nicName", "account=" + account));
                        object.put(Keys.GENDER, databaseConnection.query("user", "gender", "account=" + account));
                        object.put(Keys.AGE, databaseConnection.query("user", "age", "account=" + account));
                        object.put(Keys.LEVEL, databaseConnection.query("user", "level", "account=" + account));
                        object.put(Keys.IS_AUTHOR, "false");
                        object.put(Keys.POST_CONTENT, content);
                        object.put(Keys.ACCOUNT, account);
                        object.put(Keys.DATE, date);
                        if ("0".equals(floor1)) {
                            // content
                            object.put(Keys.IS_AUTHOR, "true");
                            object.put(Keys.PRAISE, databaseConnection.query("post", "praise", params[0]));
                        } else if ("0".equals(pid)) {
                            // author
                            object.put(Keys.FLOOR, floor1);
                            if (author.equals(account)) {
                                object.put(Keys.IS_AUTHOR, "true");
                            }
                        } else {
                            // another
                            object.put(Keys.FLOOR, floor1);
                            if (author.equals(account)) {
                                object.put(Keys.IS_AUTHOR, "true");
                            }
                            String anotherAccount = databaseConnection.query("floor", "account", "pid=" + pid);
                            object.put(Keys.ANOTHER_USER_NAME, databaseConnection.query("user", "nicName", "account=" + anotherAccount));
                            object.put(Keys.ANOTHER_CONTENT, databaseConnection.query("floor", "content", "floor=" + pid));
                        }
                        jsonArray.add(object);
                    }
                    jsonObject.put(Keys.RETURN_DATA, jsonArray);
                    jsonObject.put(Keys.MESSAGE, "请求成功");
                    jsonObject.put(Keys.STATUS, "200");
                }
                returnData = jsonObject.toString();
            } catch (Exception e) {
                e.printStackTrace();
                jsonObject.put(Keys.MESSAGE, "请求失败");
                jsonObject.put(Keys.STATUS, "400");
                returnData = jsonObject.toString();
            } finally {
                try {
                    if (resultSet != null && resultSet.isClosed()) {
                        resultSet.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 点赞
     */
    private void praisePost() {
        HashMap updateData = new JsonToHashMap().getHashMap(this.updateData, new String[] {Keys.ID, Keys.ACCOUNT, Keys.PASSWORD});
        if (updateData != null) {
            String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
            if (updateData.get(Keys.PASSWORD).equals(correctPassword)) {
                int praise = Integer.parseInt(databaseConnection.query("post", "praise", "id=" + updateData.get(Keys.ID)));
                if (databaseConnection.isExist("praise", "account", (String) updateData.get(Keys.ACCOUNT), "id=" + updateData.get(Keys.ID))) {
                    praise--;
                    databaseConnection.delete("praise", "account=" + updateData.get(Keys.ACCOUNT));
                    returnDataHashMap.put(Keys.STATUS, "201");
                    createMessage("取消点赞");
                } else {
                    databaseConnection.insert("praise",
                            new String[] {Keys.ACCOUNT, Keys.ID},
                            new String[] {(String) updateData.get(Keys.ACCOUNT), (String) updateData.get(Keys.ID)});
                    praise++;
                    returnDataHashMap.put(Keys.STATUS, "200");
                    createMessage("点赞成功");
                }
                databaseConnection.update("post", "praise", String.valueOf(praise), "id=" + updateData.get(Keys.ID));
                return;
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("点赞失败");
    }

    /**
     * 收藏
     */
    private void starPost() {
        HashMap updateData = new JsonToHashMap().getHashMap(this.updateData, new String[] {Keys.ID, Keys.ACCOUNT, Keys.PASSWORD});
        if (updateData != null) {
            String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
            if (updateData.get(Keys.PASSWORD).equals(correctPassword)) {
                ArrayList<String> stars = new ArrayList<>();
                Collections.addAll(stars, databaseConnection.query("user", "star", "account=" + updateData.get(Keys.ACCOUNT)).split(";"));
                int starCount = Integer.parseInt(databaseConnection.query("user", "starCount", "account=" + updateData.get(Keys.ACCOUNT)));
                if (databaseConnection.isExist("star", "account", (String) updateData.get(Keys.ACCOUNT))) {
                    stars.remove(updateData.get(Keys.ID).toString());
                    databaseConnection.delete("star", "account=" + updateData.get(Keys.ACCOUNT));
                    returnDataHashMap.put(Keys.STATUS, "201");
                    createMessage("取消收藏");
                    starCount--;
                } else {
                    stars.add(updateData.get(Keys.ID).toString());
                    databaseConnection.insert("star",
                            new String[] {Keys.ID, Keys.ACCOUNT},
                            new String[] {(String) updateData.get(Keys.ID), (String) updateData.get(Keys.ACCOUNT)});
                    returnDataHashMap.put(Keys.STATUS, "200");
                    createMessage("收藏成功");
                    starCount++;
                }
                databaseConnection.update("user", "starCount", String.valueOf(starCount), "account=" + updateData.get(Keys.ACCOUNT));
                StringBuilder stringBuilder = new StringBuilder();
                for (String id : stars) {
                    if (!"".equals(id)) {
                        stringBuilder.append(id).append(";");
                    }
                }
                databaseConnection.update("user", "star", stringBuilder.toString(), "account=" + updateData.get(Keys.ACCOUNT));
                return;
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("收藏失败");
    }

    /**
     * 检查是否已经收藏
     */
    private void checkStar() {
        HashMap updateData = new JsonToHashMap().getHashMap(this.updateData, new String[]{Keys.ACCOUNT, Keys.ID, Keys.PASSWORD});
        if (updateData != null) {
            String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
            if (updateData.get(Keys.PASSWORD).equals(correctPassword)) {
                if (databaseConnection.isExist("star", "account", (String) updateData.get(Keys.ACCOUNT), "id=" + updateData.get(Keys.ID))) {
                    returnDataHashMap.put(Keys.STATUS, "200");
                    createMessage("已收藏");
                    return;
                }
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("未收藏");
    }

    /**
     * 检查是否已经点赞
     */
    private void checkPraise() {
        HashMap updateData = new JsonToHashMap().getHashMap(this.updateData, new String[]{Keys.ACCOUNT, Keys.ID, Keys.PASSWORD});
        if (updateData != null) {
            String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
            if (updateData.get(Keys.PASSWORD).equals(correctPassword)) {
                if (databaseConnection.isExist("praise", "account", (String) updateData.get(Keys.ACCOUNT), "id=" + updateData.get(Keys.ID))) {
                    returnDataHashMap.put(Keys.STATUS, "200");
                    createMessage("已点赞");
                    return;
                }
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("未点赞");
    }

    /**
     * 评论
     */
    private void commentPost() {
        HashMap updateData = new JsonToHashMap().getHashMap(this.updateData,
                new String[]{Keys.ACCOUNT, Keys.PASSWORD, Keys.FLOOR, Keys.ID, Keys.POST_CONTENT});
        if (updateData != null) {
            String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
            if (updateData.get(Keys.PASSWORD).equals(correctPassword)) {
                int floor = 0;
                String sql = "SELECT * FROM floor WHERE id=" + updateData.get(Keys.ID);
                ResultSet resultSet = databaseConnection.getResultSet(sql);
                try {
                    while (resultSet.next()) {
                        floor++;
                    }
                    resultSet.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String date = null;
                String time = null;
                Calendar calendar = Calendar.getInstance();
                date = calendar.get(Calendar.YEAR) + ":" + (calendar.get(Calendar.MONTH) + 1) + ":" + calendar.get(Calendar.DATE);
                time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);

                // 改变帖子的visit
                int visit = Integer.parseInt(databaseConnection.query("post", "visit", "id=" + updateData.get(Keys.ID)));
                if (!databaseConnection.isExist("floor", "account", updateData.get(Keys.ACCOUNT).toString(), "id=" + updateData.get(Keys.ID))) {
                    visit++;
                    databaseConnection.update("post", "visit", String.valueOf(visit), "id=" + updateData.get(Keys.ID));
                }

                databaseConnection.insert("floor",
                        new String[]{
                                "id",
                                "floor",
                                "pid",
                                "content",
                                "account",
                                "date",
                                "time"},
                        new String[]{
                                (String) updateData.get(Keys.ID),
                                String.valueOf(floor),
                                (String) updateData.get(Keys.FLOOR),
                                (String) updateData.get(Keys.POST_CONTENT),
                                (String) updateData.get(Keys.ACCOUNT),
                                date,
                                time});
                int discuss = Integer.parseInt(databaseConnection.query("post", "discuss", "id=" + updateData.get(Keys.ID)));
                discuss++;
                databaseConnection.update("post", "discuss", String.valueOf(discuss), "id=" + updateData.get(Keys.ID));
                returnDataHashMap.put(Keys.STATUS, "200");
                createMessage("评论成功");

                // 增加经验值
                int exp = Integer.parseInt(databaseConnection.query("user", "exp", "account=" + updateData.get(Keys.ACCOUNT)));
                exp += 5;
                int level = 0;
                if (exp <= 1000) {
                    level = 1;
                } else if (exp <= 4000) {
                    level = 2;
                } else if (exp <= 8000) {
                    level = 3;
                } else if (exp <= 16000) {
                    level = 4;
                } else if (exp <= 32000) {
                    level = 5;
                } else if (exp <= 64000) {
                    level = 6;
                } else {
                    level = 6;
                    exp -= 5;
                }
                databaseConnection.update("user", "level", String.valueOf(level), "account=" + updateData.get(Keys.ACCOUNT));
                databaseConnection.update("user", "exp", String.valueOf(exp), "account=" + updateData.get(Keys.ACCOUNT));

                databaseConnection.update("recommend", "id", (String) updateData.get(Keys.ID), "plate='NULL'");
                // 添加到板块中的置顶
                DatabaseManager connection = DatabaseConnectionPool.getConnection();
                String plateId = databaseConnection.query("post", "plate", "id=" + updateData.get(Keys.ID));
                String sql1 = "SELECT * FROM recommend WHERE plate=" + plateId + ";";
                resultSet = connection.getResultSet(sql);
                try {
                    int count = 0;
                    while (resultSet.next()) {
                        count++;
                    }
                    resultSet.close();
                    sql = "SELECT * FROM recommend WHERE id=" + updateData.get(Keys.ID) + " AND plate=" + plateId + ";";
                    resultSet = connection.getResultSet(sql);
                    boolean isExist = resultSet.next();
                    resultSet.close();
                    if (count == 4) {
                        if (!isExist) {
                            sql = "SELECT * FROM post WHERE plate=" + plateId + " ORDER BY edit_date, edit_time DESC;";
                            resultSet = connection.getResultSet(sql);
                            String postIdForDelete = null;
                            for (int i = 0; resultSet.next() && i < 4; i++) {
                                if (i == 3) {
                                    postIdForDelete = resultSet.getString("id");
                                }
                            }
                            resultSet.close();
                            databaseConnection.delete("recommend", "id=" + postIdForDelete);
                            databaseConnection.insert("recommend", new String[]{"id", "plate"}, new String[]{(String) updateData.get(Keys.ID),
                                    databaseConnection.query("post", "plate", "id=" + updateData.get(Keys.ID))});
                        }
                    } else {
                        if (!isExist) {
                            databaseConnection.insert("recommend", new String[]{"id", "plate"}, new String[]{(String) updateData.get(Keys.ID),
                                    databaseConnection.query("post", "plate", "id=" + updateData.get(Keys.ID))});
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    DatabaseConnectionPool.close(connection);
                }
                return;
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("评论失败");
    }

    /**
     * 获取帖子的总页数
     */
    private void getPostTotalPages() {
        if (params != null) {
            int floors = -1;
            String sql = "SELECT * FROM floor WHERE " + params[0];
            ResultSet resultSet = databaseConnection.getResultSet(sql);
            try {
                while (resultSet.next()) {
                    floors++;
                }
                resultSet.close();
                int maxPages = 0;
                if (floors % 20 != 0) {
                    maxPages = floors / 20 + 1;
                } else {
                    maxPages = floors / 20;
                }
                returnDataHashMap.put(Keys.STATUS, "200");
                createMessage("请求成功");
                returnDataHashMap.put(Keys.TOTAL_PAGES, String.valueOf(maxPages));
                return;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("请求失败");
    }

    /**
     * 发表帖子
     */
    private void postPost() {
        HashMap updateData = new JsonToHashMap().getHashMap(this.updateData,
                new String[]{Keys.ID, Keys.ACCOUNT, Keys.PASSWORD, Keys.POST_CONTENT, Keys.POST_TITLE, Keys.ICON});
        if (updateData != null) {
            String correctPassword = databaseConnection.query("user", "password", "account=" + updateData.get(Keys.ACCOUNT));
            if (correctPassword.equals(updateData.get(Keys.PASSWORD))) {
                try {
                    String content = updateData.get(Keys.POST_CONTENT).toString();
                    String description = null;
                    if (((String) updateData.get(Keys.POST_CONTENT)).length() > 16) {
                        description = content.substring(0, 15);
                    } else {
                        description = content;
                    }
                    Calendar calendar = Calendar.getInstance();
                    String edit_date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE);
                    String edit_time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                    String author = databaseConnection.query("user", "nicName", "account=" + updateData.get(Keys.ACCOUNT));
                    databaseConnection.insert("post", new String[]{
                            "content",
                            "edit_date",
                            "description",
                            "icon",
                            "author",
                            "edit_time",
                            "title",
                            "plate"}, new String[]{
                            (String) updateData.get(Keys.POST_CONTENT),
                            edit_date,
                            description,
                            (String) updateData.get(Keys.ICON),
                            author,
                            edit_time,
                            (String) updateData.get(Keys.POST_TITLE),
                            (String) updateData.get(Keys.ID)});
                    String id = databaseConnection.query("post", "id",
                            "author='" + author + "' AND " + "edit_date='"
                                    + edit_date + "'" + " AND " + "edit_time='" + edit_time + "'");
                    databaseConnection.insert("floor", new String[]{
                            "id",
                            "floor",
                            "content",
                            "account",
                            "date",
                            "time"}, new String[]{id,
                            "0",
                            content,
                            (String) updateData.get(Keys.ACCOUNT),
                            edit_date,
                            edit_time});

                    // 改变帖子的visit
                    databaseConnection.update("post", "visit", "1", "id=" + id);

                    // 增加经验值
                    int exp = Integer.parseInt(databaseConnection.query("user", "exp", "account=" + updateData.get(Keys.ACCOUNT)));
                    exp += 10;
                    int level = 0;
                    if (exp <= 1000) {
                        level = 1;
                    } else if (exp <= 4000) {
                        level = 2;
                    } else if (exp <= 8000) {
                        level = 3;
                    } else if (exp <= 16000) {
                        level = 4;
                    } else if (exp <= 32000) {
                        level = 5;
                    } else if (exp <= 64000) {
                        level = 6;
                    } else {
                        level = 6;
                        exp -= 10;
                    }
                    databaseConnection.update("user", "level", String.valueOf(level), "account=" + updateData.get(Keys.ACCOUNT));
                    databaseConnection.update("user", "exp", String.valueOf(exp), "account=" + updateData.get(Keys.ACCOUNT));

                    if (databaseConnection.isExist("recommend", "plate", "'NULL'")) {
                        databaseConnection.update("recommend", "id", id, "plate='NULL'");
                    } else {
                        databaseConnection.insert("recommend", new String[]{"id", "plate"},
                                new String[]{id, "NULL"});
                    }
                    // 添加到板块中的置顶
                    DatabaseManager connection = DatabaseConnectionPool.getConnection();
                    String sql = "SELECT * FROM recommend WHERE plate=" + updateData.get(Keys.ID);
                    ResultSet resultSet = connection.getResultSet(sql);
                    try {
                        int count = 0;
                        while (resultSet.next()) {
                            count++;
                        }
                        resultSet.close();
                        resultSet.close();
                        if (count == 4) {
                            sql = "SELECT * FROM post WHERE plate=" + updateData.get(Keys.ID) + " ORDER BY edit_date DESC, edit_time DESC;";
                            resultSet = connection.getResultSet(sql);
                            String postIdForDelete = null;
                            for (int i = 0; resultSet.next() && i < 4; i++) {
                                if (i == 3) {
                                    postIdForDelete = resultSet.getString("id");
                                    System.out.println(postIdForDelete);
                                }
                            }
                            resultSet.close();
                            databaseConnection.delete("recommend", "id=" + postIdForDelete);
                            databaseConnection.insert("recommend", new String[]{"id", "plate"}, new String[]{id, (String) updateData.get(Keys.ID)});
                        } else {
                            databaseConnection.insert("recommend", new String[]{"id", "plate"}, new String[]{id, (String) updateData.get(Keys.ID)});
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        DatabaseConnectionPool.close(connection);
                    }
                    returnDataHashMap.put(Keys.STATUS, "200");
                    createMessage("发表成功");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        returnDataHashMap.put(Keys.STATUS, "400");
        createMessage("发表失败");
    }

    private void searchPost() {
        String target = null;
        if (params != null) {
            target = params[0].split("=")[1];
        }
        if (target != null) {
            target = URLDecoder.decode(target, StandardCharsets.UTF_8);
        }
        List<String> result = databaseConnection.queryAll("post", "id", "title LIKE '%" + target + "%'");
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (String id : result) {
            JSONObject object = new JSONObject();
            object.put(Keys.ID, id);
            HashMap<String, String> post = databaseConnection.gets("post", new String[]{"title", "icon", "picture",
                    "content", "visit", "discuss", "edit_date", "edit_time", "description", "author", "praise"}, "id=" + id);
            object.put(Keys.POST_TITLE, post.get(Keys.POST_TITLE));
            object.put(Keys.ICON, post.get(Keys.ICON));
            object.put(Keys.PICTURE, post.get(Keys.PICTURE));
            object.put(Keys.POST_CONTENT, post.get(Keys.POST_CONTENT));
            object.put(Keys.POST_VISIT, post.get(Keys.POST_VISIT));
            object.put(Keys.POST_DISCUSS, post.get(Keys.POST_DISCUSS));
            object.put(Keys.DATE, post.get(Keys.DATE));
            object.put(Keys.TIME, post.get(Keys.TIME));
            object.put(Keys.POST_DESCRIPTION, post.get(Keys.POST_DESCRIPTION));
            object.put(Keys.POST_AUTHOR, post.get(Keys.POST_AUTHOR));
            object.put(Keys.PRAISE, post.get(Keys.PRAISE));
            jsonArray.add(object);
        }
        jsonObject.put(Keys.RETURN_DATA, jsonArray);
        jsonObject.put(Keys.STATUS, "200");
        jsonObject.put(Keys.MESSAGE, "请求成功");
        returnData = jsonObject.toString();
    }

    /**
     * 获取用户的帖子
     */
    private void getPostsOfUser() {

    }
}
