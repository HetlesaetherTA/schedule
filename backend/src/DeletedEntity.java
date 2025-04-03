import java.util.HashMap;

public class DeletedEntity extends Entity {
    int uuid;
    String name;
    String link;
    String state;
    String children;
    HashMap<String, String> dmp;
    String depth;

    public DeletedEntity(int uuid, HashMap<String, String> dmp) {
        super(null,null,null,dmp);
        this.uuid = uuid;
        this.children = null;
        this.depth = null;
        this.depth = null;
    }
}
