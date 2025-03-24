import java.util.HashMap;

public class Project extends Entity {
    private String doneBy;
    Project(String doneBy, String name, String state, HashMap<String, String> link, HashMap<String, String> dmp) {
        super(name, state, link, dmp);
        this.doneBy = doneBy;
    }

    public String getDoneBy() {
        return doneBy;
    }

    @Override
    public String parseType() {
        return String.format("{\"Project\":{\"doneBy\": \"%s\"}}", doneBy);
    }

    @Override
    public int calculateDepth() {
        return 1;
    }

    public static Project constructFromDB(HashMap<String,String> params, String name, HashMap<String, String> link, String state, HashMap<String, String> dmp) {
        String l_doneBy = params.get("doneBy");

        return new Project(l_doneBy, name, state, link, dmp);
    }
}
