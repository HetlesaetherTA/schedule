module com.hetlesaetherta.gui_javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;
    requires java.sql;


    opens com.hetlesaetherta.gui_javafx to javafx.fxml;
    exports com.hetlesaetherta.gui_javafx;
}