import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Logging {
    DBhandler db;
    private final DBLogger dbLogger;

    Logging(DBhandler db) {
        this.db = db;
        this.dbLogger = new DBLogger(new DBLogger.BackgroundThreadRunner());
        db.addObserver(dbLogger);
    }

    public void test() {
    }


    private void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public interface DBObserver {
        void onDBChanged(String action, String[] tablesThatHasChanged, String[] tablesNewValue);
    }

    private void drawDBFrame() {
        System.out.println("drawDBFrame()");
        // TODO: create dashboard show DB in real time on console
    }


    private Map<Runnable, String> changeToDBMap = new HashMap();

    private void addChangeToLog() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("addChangeToLog()");
        // TODO: send changes to log file
    }

    public class DBLogger implements DBObserver {
        private final BackgroundThreadRunner runner;

        public DBLogger(BackgroundThreadRunner runner) {
            this.runner = runner;
        }

        @Override
        public void onDBChanged(String action, String[] updatedTables, String[] tablesNewValue) {
            Runnable logs = () -> addChangeToLog();
            changeToDBMap.put(logs, loggerStringBuilder(action, updatedTables, tablesNewValue));
            runner.add(logs);

            drawDBFrame();
        }

        private String loggerStringBuilder(String action, String[] updatedTables, String[] tablesNewValue) {
            StringBuilder str = new StringBuilder();
            str.append("---- ").append(action).append(" ----\n");
            for (String table : updatedTables) {
                str.append(table).append("\n");
            }
            str.append("---- has changed to: ----\n");
            for (String table : tablesNewValue) {
                str.append(table).append("\n");
            }

            return str.toString();
        }

        public static void addToTaskQueue(Runnable func) {

        }

        private static class BackgroundThreadRunner {
            private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
            private final Thread thread;

            public BackgroundThreadRunner() {
                thread = new Thread(() -> {
                    try {
                        while (true) {
                            Runnable task = taskQueue.take();
                            task.run();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                thread.start();
            }

            public void add(Runnable task) {
                taskQueue.add(task);
            }
        }
    }
}
