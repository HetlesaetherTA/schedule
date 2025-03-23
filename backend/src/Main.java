import java.util.Date;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        try {
            DBhandler database = new DBhandler();
            String name = "test";
            String state = "Active";
            HashMap<String,String> link = new HashMap<>();
            link.put("desc", "this is a test");
            String[] children = {"1", "2"};
            HashMap<String,String> dmp = new HashMap<>();
            dmp.put("parentUUID", "0");
            Entity test = new Project(new Date(), name, state, link, children, dmp);
            database.createEntry(test);


            System.out.println(database.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}