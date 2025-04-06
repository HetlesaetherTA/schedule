import org.junit.jupiter.api.*;

import java.util.*;

class DBhandlerTest {
    DBhandler db;

    private String getDoneBy() {
        List<String> alternatives = new ArrayList<String>();
        alternatives.add("2025-03-04T15:00:00");
        alternatives.add("2025-05-04T23:59:00");
        alternatives.add("2025-11-04T19:40:00");
        alternatives.add("2025-25-05T12:32:00");

        float seed = new Random().nextFloat();
        int index = (int)(seed * alternatives.size() - 1);
        return alternatives.get(index);
    }

    private String getName() {
        return "test";
    }

    private HashMap<String, String> getLink() {
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
        exampleEntities.add(new Project(getDoneBy(), getName(), getState(), getLink(), getDmp()));
        return exampleEntities.get(0);
    }

    private Entity getDeletedEntity(int uuid) {
        Entity entity = new Project(null,null,null,null,new HashMap<>());
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
        db.delete(2, true);

        System.out.println(db.toString() + "\n\n\n\n");

        Entity[] entityArray = new Entity[] {test, test2};

        Entity removedEntity = db.read(2);

        Assertions.assertEquals(removedEntity.toStringWithoutDMP(), new DeletedEntity(1, new HashMap<>()).toStringWithoutDMP());
    }

    @Test
    void updateEntity() {
        Entity test = db.create(getExampleEntity());
        Entity test2 = getExampleEntity();

        int id = test.getUUID();

        System.out.println(db.toString() + "\n\n");
        Assertions.assertEquals(test.toStringWithoutDMP(), db.read(id).toStringWithoutDMP());
        test2 = db.update(id, test2);
        System.out.println(db.toString());
        Assertions.assertEquals(test2.toStringWithoutDMP(), db.read(id).toStringWithoutDMP());
    }

    @Test
    void iterateThoughDB() {
        Entity test = db.create(getExampleEntity());
        Entity test2 = db.create(test, getExampleEntity());
        Entity test3 = db.create(getExampleEntity());

        Entity[] entityArray = new Entity[]{test, test2, test3};

        for (Entity entity : db) {
            System.out.println(entity.toStringWithoutDMP());
            Assertions.assertEquals(entity.toStringWithoutDMP(), entityArray[entity.getUUID()-1].toStringWithoutDMP());
        }
    }
    @Test
    void deleteEntityWithChildren() {
        Entity test = db.create(getExampleEntity());
        Entity test2 = db.create(test, getExampleEntity());
        Assertions.assertThrows(IllegalStateException.class, () -> db.delete(1, false));
    }

    @Test
    void recursiveDelete() {
        Entity test = db.create(getExampleEntity());
        Entity test2 = db.create(test, getExampleEntity());
        Entity test3 = db.create(test2, getExampleEntity());

        db.delete(1, true);

        for (Entity entity : db) {
            Assertions.assertEquals(entity.toStringWithoutDMP(), getDeletedEntity(entity.getUUID()).toStringWithoutDMP());
        }

        System.out.println(db.toString());
    }
}

