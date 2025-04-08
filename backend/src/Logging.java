import org.eclipse.jetty.websocket.server.WebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {
    DBhandler db;
    private final DBLogger dbLogger;
    private static final Logger logger = Logger.getLogger(Logging.class.getName());
    private static FileHandler fileHandler;

    Logging(DBhandler db) {
        this.db = db;
        this.dbLogger = new DBLogger(new DBLogger.BackgroundThreadRunner());
        db.addObserver(dbLogger);
        drawDBFrame();

        try {
            fileHandler = new FileHandler(Util.Config.getLogsPath(), true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.INFO);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Could not open log file");
            fileHandler = null;
            e.printStackTrace();
        }
    }

    private void clearConsole() {
        try {
            String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Fallback: if we can't clear, just print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    public interface DBObserver {
        void onDBChanged(String action, String[] tablesThatHasChanged, String[] tablesNewValue);
    }

    private void drawDBFrame() {
        clearConsole();
        System.out.println(db.toStringWithoutDmp());
        String[] dmps = db.getAllDmp();

        for (int i=0; i<dmps.length; i++) {
            System.out.println(i + ": " + dmps[i]);
        }
    }


    private Map<Runnable, String> changeToDBMap = new HashMap();

    private void addChangeToLog(String log) {
        try {
            logger.info(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DBLogger implements DBObserver {
        private final BackgroundThreadRunner runner;

        public DBLogger(BackgroundThreadRunner runner) {
            this.runner = runner;
        }

        @Override
        public void onDBChanged(String action, String[] updatedTables, String[] tablesNewValue) {
            if (fileHandler != null) {
                Runnable logs = () -> {
                    addChangeToLog(loggerStringBuilder(action, updatedTables, tablesNewValue));
                };
                changeToDBMap.put(logs, loggerStringBuilder(action, updatedTables, tablesNewValue));
                runner.add(logs);
            }

            runner.add(() -> drawDBFrame());
        }

        private String loggerStringBuilder(String action, String[] updatedTables, String[] tablesNewValue) {
            StringBuilder str = new StringBuilder();
            str.append("---- ").append(action).append(" ----\n");
            for (int i = 0; i < updatedTables.length; i++) {
                str.append(i+1 + ": " + updatedTables[i] + "\n");
            }

            str.append("---- HAS CHANGED ----\n");
            for (int i = 0; i < tablesNewValue.length; i++) {
                str.append(i+1 + ": " + tablesNewValue[i] + "\n");
            }
            str.append("---- CLOSED ----\n");

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
