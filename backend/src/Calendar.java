import java.util.HashMap;

public class Calendar extends Entity {
   static String startTime;
   String endTime;
   Calendar(String startTime, String endTime, String name, String state, HashMap<String, String> link, HashMap<String, String> dmp) {
       super(name, state, link, dmp);
       this.startTime = startTime;
       this.endTime = endTime;
   }

   @Override
   public String parseType() {
       return "{\"Calendar\":{\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\"}}";
   }

   public static Calendar constructFromDB(HashMap<String,String> params, int uuid, String name, HashMap<String, String> link, String state, int depth) {
       String l_startTime = params.get("startTime");
       String l_endTime = params.get("endTime");
       return new Calendar(l_startTime, l_endTime, name, state, link, new HashMap<String, String>());
    }
}
