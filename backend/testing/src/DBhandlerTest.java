import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

class DBhandlerTest {
    DBhandler db;

    private String getDoneBy() {
        return "2025-25-03T19:00:00";
    }

    private String getName() {
        return "test";
    }

    private HashMap<String, String> getLink() {
        HashMap<String, String> link = new HashMap<>();
        link.put("google", "https://www.google.com/");
        link.put("desc", "start this on the 20th");
        return link;
    }

    private String getState() {
        return "Active";
    }

    private HashMap<String, String> getDmp() {
        HashMap<String, String> dmp1 = new HashMap<>();
        dmp1.put("frontend_dmp", "info");
        return dmp1;
    }

    private Entity getExampleEntity() {
        List<Entity> exampleEntities = new java.util.ArrayList<>(List.of());
        exampleEntities.add(new Project(getDoneBy(), getName(), getState(), getLink(), getDmp()));
        return exampleEntities.get(0);

    }

    private Entity getDeletedEntity(int uuid) {
        Entity entity = new Project(null,null,null,null,null);
        entity.setUUID(uuid);
        return entity;
    }

    @BeforeEach
    void setup() {
        try {
            db = new DBhandler(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void cleanup() {
        db.cleanUpTest();
    }

    @Test
    void createToRoot() {
        Entity test = db.create(getExampleEntity());
        Entity test2 = db.read(1);
        System.out.println(test2 + "\n\n\n\n");

        System.out.println(test.toString() + "\n" + test2.toString());
        System.out.println(db.toString());
        Assertions.assertEquals(test.toStringWithoutDMP(), test2.toStringWithoutDMP());
    }

    @Test
    void createToChild() {
        Entity test = db.create(getExampleEntity());
        Entity test2 = db.create(test, getExampleEntity());

        String children = test.getChildren();

        String[] childrenArray = children.split(";");

        try {
            for (String child : childrenArray) {
                if (db.read(Integer.parseInt(child)).toString().equals(test2.toString())) {
                    Assertions.assertEquals(db.read(Integer.parseInt(child)).toString(), test2.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void create150Requests() {
        int i = 0;
        while (i < 50) {
            Entity test = db.create(getExampleEntity());
            Entity test2 = db.create(test, getExampleEntity());
            Entity test3 = db.create(test2, getExampleEntity());
            i++;
        }

        System.out.println(db.toString());
        Assertions.assertEquals(151, db.size());
    }
    @Test
    void deleteEntry() {
        Entity test = db.create(getExampleEntity());
        Entity test2 = db.create(test, getExampleEntity());
        db.delete(2);

        System.out.println(db.toString() + "\n\n\n\n");

        Entity[] entityArray = new Entity[] {test, test2};

        Entity removedEntity = db.read(2);

        Assertions.assertEquals(removedEntity.toStringWithoutDMP(), new DeletedEntity(1, new HashMap<>()).toStringWithoutDMP());
    }
}

