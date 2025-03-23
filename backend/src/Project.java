import java.util.Date;
import java.util.HashMap;

public class Project extends Entity {
    private Date doneBy;
    Project(Date doneBy, String name, String state, HashMap<String, String> link, String[] children, HashMap<String, String> dmp) {
        super(name, state, link, children, dmp);
        this.doneBy = doneBy;
    }

    public Date getDoneBy() {
        return doneBy;
    }

    @Override
    public String parseType() {
        return String.format("""
                {
                    "calender": {
                    "doneBy": "%s"
                    }
                }
                """, doneBy);
    }
}
