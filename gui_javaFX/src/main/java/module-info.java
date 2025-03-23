module com.hetlesaetherta.gui_javafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.hetlesaetherta.gui_javafx to javafx.fxml;
    exports com.hetlesaetherta.gui_javafx;
}