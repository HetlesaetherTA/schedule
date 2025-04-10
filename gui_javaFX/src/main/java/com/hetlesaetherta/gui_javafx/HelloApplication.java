package com.hetlesaetherta.gui_javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("archive.fxml"));

        Parent root = fxmlLoader.load();
        AppController controller = fxmlLoader.getController();

        Scene scene = new Scene(root, 650, 1000);
        stage.setTitle("test");
        stage.setScene(scene);
        stage.show();

        controller.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
