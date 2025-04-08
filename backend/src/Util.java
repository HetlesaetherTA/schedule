import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Method;
import java.util.*;

public class Util {
    public class Config {
        public static String getLogsPath() {
            return "schedule.log";
        }
    }
    public static JsonObject getDeletedEntityJson(int uuid, JsonObject dmp) {
        return new Gson().fromJson(
                "{\"uuid\":" + uuid + "," +
                "\"name\":null," +
                "\"type\":null," +
                "\"link\":null,"+
                "\"state\":null," +
                "\"depth\":null," +
                "\"children\":null," +
                "\"dmp\":" + dmp + "}", JsonObject.class);
    }

    public static String getDoneBy() {
        List<String> alternatives = new ArrayList<String>();
        alternatives.add("2025-03-04T15:00:00");
        alternatives.add("2025-05-04T23:59:00");
        alternatives.add("2025-11-04T19:40:00");
        alternatives.add("2025-25-05T12:32:00");

        float seed = new Random().nextFloat();
        int index = (int)(seed * alternatives.size() - 1);
        return alternatives.get(index);
    }

    public static String getName() {
        return "test";
    }

    public static HashMap<String, String> getLink() {
        HashMap<String, String> alternatives = new HashMap<>();
        alternatives.put("google", "https://www.google.com/");
        alternatives.put("desc", "start this on the 20th");
        alternatives.put("priority", "high");
        alternatives.put("location", "R1");
        alternatives.put("sign in link", "www.youtube.com");

        float seed = new Random().nextFloat();
        int index = (int)(seed * alternatives.size());
        List<String> keys = new ArrayList<>(alternatives.keySet());
        HashMap<String, String> link = new HashMap<>();
        link.put(keys.get(index), alternatives.get(keys.get(index)));
        return link;
    }

    public static String getState() {
        return "Active";
    }

    public static HashMap<String, String> getDmp() {
        HashMap<String, String> dmp1 = new HashMap<>();
        dmp1.put("frontend_dmp", "info");
        return dmp1;
    }

    public static Entity getExampleEntity() {
        List<Entity> exampleEntities = new java.util.ArrayList<>(List.of());
        exampleEntities.add(new Project(getDoneBy(), getName(), getState(), getLink(), getDmp()));
        exampleEntities.add(new Project(getDoneBy(), getName(), getState(), getLink(), getDmp()));
        return exampleEntities.get(0);
    }

    public static Entity jsonToEntity(JsonObject json) {
        if (json == null) {
            return null;
        }

        Entity entity;

        int uuid;
        String name;
        JsonObject type;
        JsonObject link;
        String state;
        int depth;
        String children;
        JsonObject dmp;

        String className;
        HashMap<String, String> typeParams;

        try {
            // UUID, depth and children is missing in json from frontend and therefore
            // optional, this is because they're explicitly calculated on the server
            uuid = json.has("uuid") ? json.get("uuid").getAsInt() : -1;
            dmp = json.getAsJsonObject("dmp");

            if (json.equals(Util.getDeletedEntityJson(uuid, dmp))) {
                return new DeletedEntity(uuid, jsonObjectToHashMap(dmp));
            }

            if (uuid == 0) {
                return new Root();
            }

            depth = json.has("depth") ? json.get("depth").getAsInt() : 0;
            children = json.has("depth") ? json.get("children").getAsString() : "";

            type = json.getAsJsonObject("type");
            name = json.get("name").getAsString();
            link = json.getAsJsonObject("link");
            state = json.get("state").getAsString();

            className = type.keySet().iterator().next();
            typeParams = jsonObjectToHashMap(type.getAsJsonObject(className));
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON object is not valid " + e);
        }

        try {
            Class<? extends Entity> clazz = Entity.classMap.get(className);
            Method factoryMethod = clazz.getMethod("constructFromDB", HashMap.class, String.class, HashMap.class, String.class, HashMap.class);

            // gives all necesary info to Entity subclass and let's it handle params on a class to class level.
            entity = (Entity) factoryMethod.invoke(null, typeParams, name, jsonObjectToHashMap(link), state, jsonObjectToHashMap(dmp));

            entity.setDepth(depth);
            entity.setChildren(children);
            entity.setUUID(uuid);
        } catch (Exception e) {
            throw new NoSuchMethodError(className + " might not exist or doesn't implement constructFromDB correctly\n" + e);
        }
        return entity;
    }

    public static HashMap<String, String> jsonObjectToHashMap(JsonObject jsonObject) {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getAsString());
        }
        return map;
    }

    public static JsonObject packageJsonForAPI(JsonObject[] jsonArray) {
        JsonObject jsonObject = new JsonObject();

        for (JsonObject json : jsonArray) {
            jsonObject.add(json.get("uuid").getAsString(), json);
        }

        return jsonObject;
    }

    public static JsonObject packageJsonForAPI(JsonObject json) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(json.get("uuid").getAsString(), json);
        return jsonObject;
    }
}
