import java.util.HashMap;

public class Root extends Entity {
    Root() {
        super("root", "Active", new HashMap<String, String>(), new HashMap<String,String>());
    }

    @Override
    public String parseType() {
        return "{root:{}}";
    }
}
