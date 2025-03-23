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

import java.sql.*;
import com.google.gson.Gson;
import java.util.HashMap;

public class DBhandler {
    Connection conn;
    public DBhandler() throws SQLException {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:state.db");
            Statement stmt = conn.createStatement();

            stmt.executeUpdate(
                    """
            CREATE TABLE IF NOT EXISTS `entities`(
                uuid INTEGER,
                name TEXT,
                type TEXT,
                link TEXT,
                state TEXT,
                depth INTEGER,
                children TEXT,
                dmp TEXT
            )"""
            );


            stmt.close();

            System.out.println(generateUUID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int generateUUID() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(uuid) FROM `entities`");
            return rs.next() ? rs.getInt(1) + 1 : 0; // start at 0 if DB is empty
        } catch (Exception e) {
            e.printStackTrace();
            return -1; }
    }

    public Entity get(int uuid) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    """
                    SELECT * FROM entities WHERE uuid = ?
                            """);

            stmt.setInt(1, uuid);
            ResultSet rs = stmt.executeQuery();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Entity getParent(Entity entity) {
        return null;
    }

    // all entries must have a parent, parent must therefore be
    // defined at creation. Lowest level Entries has "root" as parent.
    public Entity createEntry(Entity parent, Entity newItem) {
        int uuid = generateUUID();

        Gson gson = new Gson(); // json library

        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO entities VALUES (?,?,?,?,?,?,?,?)");
            stmt.setInt(1, uuid);
            stmt.setString(2,newItem.getName());
            stmt.setString(3, newItem.parseType());
            stmt.setString(4, gson.toJson(newItem.getLink()));
            stmt.setString(5,newItem.getState());
            stmt.setInt(6,newItem.getDepth());
            stmt.setString(7, String.join(";", newItem.getChildren()));
            stmt.setString(8, gson.toJson(newItem.getDmp()));

            stmt.executeUpdate();
            createRelationship(parent, newItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commitChange(Entity entry) {

    }



    private void createRelationship(Entity parent, Entity child) {
        parent.add
        try {

            PreparedStatement stmt = conn.prepareStatement("""
                UPDATE entities
                SET children=children || ';' || uuid
                WHERE uuid = ?
    """);

        stmt.setInt(1, Integer.parseInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeEntry(int uuid) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
            DELETE FROM entities
            WHERE uuid = ?
""");
            stmt.setInt(1, uuid);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM `entities`");

            while (rs.next()) {
                sb.append(
                        rs.getInt("uuid") + " | " +
                        rs.getString("name") + " | " +
                        rs.getString("type") + " | " +
                        rs.getString("link") + " | " +
                        rs.getString("state") + " | " +
                        rs.getInt("depth") + " | " +
                        rs.getString("children") + " | " +
                        rs.getString("dmp") + "\n"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        };

        return sb.toString();
    }
}
