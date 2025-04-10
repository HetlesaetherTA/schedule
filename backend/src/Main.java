public class Main {
    public static void main(String[] args) {
        try {
            DBhandler db = new DBhandler(false);
             Logging log = new Logging(db);

            HTTPhandler.start("127.0.0.1", 8080, db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}