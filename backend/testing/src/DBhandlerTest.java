import org.junit.jupiter.api.*;

import java.util.*;
import com.google.gson.JsonObject;

class DBhandlerTest {
    DBhandler db;

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
        Entity test = db.create(Util.getExampleEntity());
        Entity test2 = db.read(1);
        System.out.println(test2 + "\n\n\n\n");

        System.out.println(test.toString() + "\n" + test2.toString());
        System.out.println(db.toString());
        Assertions.assertEquals(test.toStringWithoutDMP(), test2.toStringWithoutDMP());
    }

    @Test
    void createToChild() {
        Entity test = db.create(Util.getExampleEntity());
        Entity test2 = db.create(test, Util.getExampleEntity());

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
            Entity test = db.create(Util.getExampleEntity());
            Entity test2 = db.create(test, Util.getExampleEntity());
            Entity test3 = db.create(test2, Util.getExampleEntity());
            i++;
        }

        System.out.println(db.toString());
        Assertions.assertEquals(151, db.size());
    }
    @Test
    void deleteEntry() {
        Entity test = db.create(Util.getExampleEntity());
        Entity test2 = db.create(test, Util.getExampleEntity());
        db.delete(test2.getUUID(), true);

        System.out.println(db.toString() + "\n\n\n\n");

        Entity removedEntity = db.read(test2.getUUID());

        Entity deleted = new DeletedEntity(2, new HashMap<String, String>());
        System.out.println(deleted);

        Assertions.assertEquals(removedEntity.toStringWithoutDMP(), new DeletedEntity(test2.getUUID(), new HashMap<>()).toStringWithoutDMP());
    }

    @Test
    void updateEntity() {
        Entity test = db.create(Util.getExampleEntity());
        Entity test2 = Util.getExampleEntity();

        int id = test.getUUID();

        System.out.println(db.toString() + "\n\n");
        Assertions.assertEquals(test.toStringWithoutDMP(), db.read(id).toStringWithoutDMP());
        test2 = db.update(id, test2);
        System.out.println(db.toString());
        Assertions.assertEquals(test2.toStringWithoutDMP(), db.read(id).toStringWithoutDMP());
    }

    @Test
    void iterateThoughDB() {
        Entity test = db.create(Util.getExampleEntity());
        Entity test2 = db.create(test, Util.getExampleEntity());
        Entity test3 = db.create(Util.getExampleEntity());

        Entity[] entityArray = new Entity[]{test, test2, test3};

        for (Entity entity : db) {
            System.out.println(entity.toStringWithoutDMP());
            Assertions.assertEquals(entity.toStringWithoutDMP(), entityArray[entity.getUUID()-1].toStringWithoutDMP());
        }
    }
    @Test
    void deleteEntityWithChildren() {
        Entity test = db.create(Util.getExampleEntity());
        Entity test2 = db.create(test, Util.getExampleEntity());
        Assertions.assertThrows(IllegalStateException.class, () -> db.delete(1, false));
    }

    @Test
    void recursiveDelete() {
        Entity test = db.create(Util.getExampleEntity());
        Entity test2 = db.create(test, Util.getExampleEntity());
        Entity test3 = db.create(test2, Util.getExampleEntity());
        Entity test4 = db.create(test3, Util.getExampleEntity());
        Entity test5 = db.create(test4, Util.getExampleEntity());
        db.create(test5, Util.getExampleEntity());

        db.delete(test.getUUID(), true);

        System.out.println(db.toString());

        for (Entity entity : db) {
            Assertions.assertEquals(entity.toStringWithoutDMP(), new DeletedEntity(entity.getUUID(), new HashMap<>()).toStringWithoutDMP());
        }
    }

    @Test
    void testIllegalInputs() {
        Entity test = db.create(Util.getExampleEntity());

        Assertions.assertEquals(null, db.create(10, Util.getExampleEntity()));
        Assertions.assertEquals(null, db.create(Util.getExampleEntity(), test));
        Assertions.assertEquals(null, db.read(10));
        Assertions.assertEquals(null, db.read(-1));
        Assertions.assertEquals(null, db.readAsJson(10));
        Assertions.assertEquals(null, db.readAsJson(-1));
        Assertions.assertEquals(new Root().toStringWithoutDMP(), db.read(0).toStringWithoutDMP());
        Assertions.assertEquals(null, db.update(45, test));
        Assertions.assertEquals(null, db.delete(45, false));

    }
}

