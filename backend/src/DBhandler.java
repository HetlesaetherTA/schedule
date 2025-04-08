// ---------------------------------------------------------------
// |  uuid  | name |  type  |  link  | state | depth | children |  dmp  |
// |0 (root)|String| {type} | {link} |String | 0     | '1;2;3'  | {dmp} |
// |1       |String| {type} | {link} |String | 1     | '4'      | {dmp} |
// |2       |String| {type} | {link} |String | 0     | '5,6'    | {dmp} |
// |3       |String| {type} | {link} |String | 1     | ''       | {dmp} |
// |4       |String| {type} | {link} |String | 2     | ''       | {dmp} |
//
// json, type: properties ->
// {
//  'Calender': {
//      'startTime' : '2025-03-23T18:00:00',
//      'endTime'   : '2025-03-23T19:00:00',
//      'location'  : 'R1'
//   };
//  };
//
// json, link:
// {
//  'description'  : 'on 3rd of april, start look at part 1',
//  'assignment'   : 'https://ntnu.no/example_assignment/',
// }
//
// json, dmp
// {
//  'creationTime' : '2025-03-19T13:43:19',
//  'createdBy'    : 'BraveBrowser',
//  'actionHistory': {'2025-03-19T13:53:09': 'status:active->hold[2025-03-22T18:00:00]'
//  ...
// }

import java.lang.reflect.Method;
import java.sql.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DBhandler implements Iterable<Entity> {
    Connection conn;
    private List<Logging.DBObserver> observers = new ArrayList<>();

    private void notifyObservers(String action, String[] updatedTables, String[] tablesNewValue) {
        for (Logging.DBObserver observer : observers) {
            observer.onDBChanged(action, updatedTables, tablesNewValue);
        }
    }

    public void addObserver(Logging.DBObserver observer) {
        observers.add(observer);
    }

    public DBhandler(boolean isProd) throws SQLException {
        String url = isProd ? "jdbc:sqlite:state.db" : "jdbc:sqlite:test.db";

        try {
            conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `entities` (
                    uuid INTEGER,
                    name TEXT,
                    type TEXT,
                    link TEXT,
                    state TEXT,
                    depth INTEGER,
                    children TEXT,
                    dmp TEXT)
                """);

                try (ResultSet rs = stmt.executeQuery("""
                    SELECT * FROM `entities`
                    WHERE uuid = 0
                """)) {

                    if (!rs.next()) {
                        injectEntity(new Root());
                    }
                } catch (Exception e) {
                    throw new SQLException("Could not inject root\n" + e);
                }
            } catch (Exception e) {
                throw new SQLException("Could not construct database\n" + e);
            }
        } catch (Exception e) {
            throw new SQLException("Could not connect to database\n" + e);
        }
    }

    @Override
    public Iterator<Entity> iterator() {
        return readAll().iterator();
    }

    public Entity[] createFromArray(Entity[] entities) {
        for (Entity e : entities) {
            create(e);
        }
        return entities;
    }

    public Entity create(int parent_uuid, Entity newItem) {
        if (!dbContains(parent_uuid)) {
            return null;
        }

        try (PreparedStatement stmt = conn.prepareStatement("""
            SELECT depth FROM `entities` WHERE uuid= ?
        """)) {
            stmt.setInt(1, parent_uuid);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    newItem.setDepth(rs.getInt("depth") + 1);
                }
                injectEntity(newItem);
                createRelationship(parent_uuid, newItem);
                notifyObservers("CREATE", new String[]{read(parent_uuid).toString()}, new String[]{newItem.toString()});
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create entity (something wrong with newItem) \n" + newItem, e);
        }

        return newItem;
    }


    public Entity create(Entity parent, Entity newItem) {
        if (!dbContains(parent.getUUID())) {
            return null;
        }

        try {
            newItem = create(parent.getUUID(), newItem);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create entity (something wrong with newItem) \n" + newItem, e);
        }
        parent.sync(this);
        return newItem;
    }

    // createEntry without parent makes root the parent.
    public Entity create(Entity newItem) {
        try {
            create(0, newItem);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create entity \n" + newItem, e);
        }
        return newItem;
    }

    public Entity read(int uuid) {
        return read(uuid, false);
    }

    public Entity read(int uuid, Boolean withDmp) {
        Entity returnValue = Util.jsonToEntity(readAsJson(uuid));
        if (returnValue == null) {
            return null;
        }

        if (!withDmp) {
            returnValue.clearDmp();
        }

        return returnValue;
    }

    public JsonObject[] readAllAsJson() {
        JsonObject[] returnValue = new JsonObject[size()];

        for (int i = 0; i < size(); i++) {
            returnValue[i] = readAsJson(i);
        }

        return returnValue;
    }

    public JsonObject readAsJson(int uuid) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `entities` WHERE uuid = ?")) {
            stmt.setInt(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    // uuid not in DB
                    return new JsonObject();
                }

                // get Name
                String name = rs.getString("name");

                // handle state
                String state = rs.getString("state");

                // get and handle dmp
                JsonObject dmp = JsonParser.parseString(rs.getString("dmp")).getAsJsonObject();

                if (state == null) {
                    return Util.getDeletedEntityJson(uuid, dmp);
                }

                // handle link
                JsonObject link = JsonParser.parseString(rs.getString("link")).getAsJsonObject();

                // handle depth
                int depth = rs.getInt("depth");

                // get children
                String children = rs.getString("children");

                // get and handle type
                JsonObject type = JsonParser.parseString(rs.getString("type")).getAsJsonObject();

                JsonObject returnValue = new JsonObject();
                returnValue.addProperty("uuid", uuid);
                returnValue.addProperty("name", name);
                returnValue.add("type", type);
                returnValue.add("link", link);
                returnValue.addProperty("state", state);
                returnValue.addProperty("depth", depth);
                returnValue.addProperty("children", children);
                returnValue.add("dmp", dmp);

                return returnValue;
            }
            } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonObject();
    };

    public List<Entity> readAll() {
        List<Entity> entities = new ArrayList<>();

        for (int i=1; i < size(); i++) {
            entities.add(read(i));
        }
        return entities;
    }


    public int size() {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `entities`")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean dbContains(int uuid) {
        try (PreparedStatement stmt = conn.prepareStatement("""
        SELECT 1
        FROM entities
        WHERE uuid = ?
        LIMIT 1
        """)) {
            stmt.setInt(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }   catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    };


    public Entity update(int uuid, Entity entry) {
        Gson gson = new Gson();

        if (!dbContains(uuid)) {
            return null;
        }


        try (PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE `entities`
                    SET name = ?,
                    link = ?,
                    state = ?
                    WHERE uuid = ?
                    """)) {
            stmt.setString(1, entry.getName());
            stmt.setString(2, gson.toJson(entry.getLink()));
            stmt.setString(3, entry.getState());
            stmt.setInt(4, uuid);
            pushDmp(uuid, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), read(uuid).toStringWithoutDMP());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to update entity with uuid: " + uuid + "\n" + entry.toString(), e);
        }
        return read(uuid);
    }

    private void pushDmp(int uuid, String key, String value) {
        Gson gson = new Gson();

        Entity entity = read(uuid, true);
        HashMap<String, String> dmp = entity.getDmp();
        dmp.put(key, value);
        try (PreparedStatement stmt = conn.prepareStatement("""
            UPDATE `entities`
            SET dmp = ?
            WHERE uuid = ?
        """);) {
            stmt.setString(1, gson.toJson(dmp));
            stmt.setInt(2, uuid);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Entity delete(int uuid, boolean recursive) {
        if (uuid == 0) {
            throw new IllegalArgumentException("root cannot be deleted");
        }
        Gson gson = new Gson();

        Entity removedEntity = read(uuid, true);

        if (removedEntity == null) {
            return null;
        }

        Integer[] childrenUUIDs = Arrays.stream(removedEntity.getChildren().split(";"))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toArray(Integer[]::new);

        if (!recursive && childrenUUIDs.length > 0) {
            throw new IllegalStateException("Recursive deletion not checked and entity has children");
        }

        if (recursive) {
            for (int entityUUID : childrenUUIDs) {
                delete(entityUUID, true);
            }
        }

        pushDmp(uuid, "deleted", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try (PreparedStatement stmt = conn.prepareStatement("""
            UPDATE `entities`
            SET name = NULL,
                type = NULL,
                link = NULL,
                state = NULL,
                depth = NULL,
                children = NULL
            WHERE uuid = ?
        """);) {
            stmt.setInt(1, uuid);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return read(uuid);
    }

    public void cleanUpTest() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS `entities`");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int generateUUID() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(uuid) FROM `entities`");) {
            return rs.next() ? rs.getInt(1) + 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    private void injectEntity(Entity newItem) {
        if (newItem instanceof Root) {
            newItem.setUUID(0);
        } else {
            newItem.setUUID(generateUUID());
        }

        Gson gson = new Gson();

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO entities VALUES (?,?,?,?,?,?,?,?)")) {
            stmt.setInt(1, newItem.getUUID());
            stmt.setString(2, newItem.getName());
            stmt.setString(3, newItem.parseType());
            stmt.setString(4, gson.toJson(newItem.getLink()));
            stmt.setString(5, newItem.getState());
            stmt.setInt(6, newItem.getDepth());
            stmt.setString(7, String.join(";", newItem.getChildren()));
            stmt.setString(8, gson.toJson(newItem.getDmp()));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // all entries must have a parent, parent must therefore be
    // defined at creation. Lowest level Entries has "root" as parent.
    private void createRelationship(int uuid, Entity child) {
        String currentChildren = "";

        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT children FROM entities WHERE uuid = ?
        """)) {

            stmt.setInt(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    currentChildren = rs.getString("children");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (String childUUID : currentChildren.split(";")) {
                if (String.valueOf(child.getUUID()).equals(childUUID)) {
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE entities
                SET children = ?
                WHERE uuid = ?
        """)) {
            stmt.setString(1, currentChildren+String.valueOf(child.getUUID())+";");
            stmt.setInt(2, uuid);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM `entities`")) {

                ResultSetMetaData meta = rs.getMetaData();

                List<String[]> rows = new ArrayList<>();
                int[] columnWidths = new int[meta.getColumnCount()];

                // Add headers
                String[] headers = new String[meta.getColumnCount()];
                for (int i = 0; i < headers.length; i++) {
                    headers[i] = meta.getColumnName(i + 1);
                    columnWidths[i] = headers[i].length();
                }

                rows.add(headers);

                // Add body
                while (rs.next()) {
                    String[] row = new String[meta.getColumnCount()];
                    for (int i = 0; i < row.length; i++) {
                        row[i] = rs.getString(headers[i]) != null ? rs.getString(headers[i]) : "NULL";
                        columnWidths[i] = Math.max(columnWidths[i], row[i].length());
                    }
                    rows.add(row);
                }

                for (String[] row : rows) {
                    for (int i = 0; i < row.length; i++) {
                        sb.append(pad(row[i], columnWidths[i])).append(" | ");
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        };
        return sb.toString();
    }

    private static String pad(String s, int width) {
        return String.format("%-" + width + "s", s);
    }
}
