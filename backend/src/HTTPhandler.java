import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static spark.Spark.*;

public class HTTPhandler {
    public static void start(String ip, int port, DBhandler db) {
        port(port);

        get("entities/:uuid", (req, res) -> {
            res.type("application/json");
            int uuid = Integer.parseInt(req.params(":uuid"));
            JsonObject returnValue = db.readAsJson(uuid);
            if (!returnValue.isEmpty()) {
                return Util.packageJsonForAPI(returnValue);
            } else {
                res.status(404);
                return null;
            }
        });

        get("entities", (req, res) -> {
            res.type("application/json");
            JsonObject[] returnValue = db.readAllAsJson();
            if (returnValue != null) {
                return Util.packageJsonForAPI(returnValue);
            } else {
                res.status(404);
                return null;
            }
        });

        post("entities/:uuid", (req, res) -> {
            try {
                JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
                int parentUUID = Integer.parseInt(req.params(":uuid"));

                Entity parent = db.read(parentUUID);
                Entity child = Util.jsonToEntity(body);

                parent.createChild(child, db);
                return Util.packageJsonForAPI(db.readAsJson(child.getUUID()));
            } catch (Exception e) {
                e.printStackTrace();
                res.status(400);
                return null;
            }
        });

        put("entities/:uuid", (req, res) -> {
            res.type("application/json");
            try {
                JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
                int uuid = Integer.parseInt(req.params(":uuid"));

                Entity oldItem = db.read(uuid);
                Entity newItem = Util.jsonToEntity(body);

                oldItem.replaceInDB(newItem, db);
                return Util.packageJsonForAPI(body.getAsJsonObject(), uuid);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(404);
                return null;
            }
        });

        delete("entities/:uuid", (req, res) -> {
            try {
                res.type("application/json");
                int uuid = Integer.parseInt(req.params(":uuid"));
                String param = req.queryParams("recursive");
                boolean recursive = param != null && param.equals("true");

                Entity deletedEntity = db.delete(uuid, recursive);

                if (deletedEntity == null) {
                    throw new Exception();
                }
                return "SUCCESS";
            } catch (IllegalStateException e) {
                res.status(400);
                return "BAD REQUEST: non-recursive delete on entity with Children";
            } catch (Exception e) {
                res.status(404);
                return "Illegal Delete";
            }
        });
    }
}
