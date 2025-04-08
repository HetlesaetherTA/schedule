import java.lang.reflect.Method;
import java.util.HashMap;

public class Root extends Entity {
    Root() {
        super("root", "Active", new HashMap<String, String>(), new HashMap<String,String>());
        setUUID(0);
    }

    @Override
    public String parseType() {
        return "{root:{}}";
    }

    public Root constructFromDB() {
        return this;
    }
}
