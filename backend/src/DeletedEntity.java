import java.util.HashMap;

public class DeletedEntity extends Entity {
    String depth;

    public DeletedEntity(int uuid, HashMap<String, String> dmp) {
        super(null,null,null,dmp);
        this.uuid = uuid;
        this.children = null;
        this.depth = null;
    }
}
