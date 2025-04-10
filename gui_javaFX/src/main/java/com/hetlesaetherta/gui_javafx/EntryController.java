package com.hetlesaetherta.gui_javafx;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class EntryController {
    public Label linkLabel;
    public Label nameLabel;
    public Label stateLabel;
    public Label timeUntilExpLabel;

    private String uuid;
    private AppController appController;

    @FXML
    public Button backButton;

    public void addAppController(AppController appController) {
        this.appController = appController;
    }

    public void disableButton() {
        backButton.setVisible(false);
    }

    public void constructEntry(JsonObject e) {
        if (!e.get("type").getAsJsonObject().has("Project")) {
            return;
        }

        String nameVal;
        String stateVal;
        String linkVal;
        String timeUntilExpVal;
        LocalDateTime time;

        // Handle name
        if (e.get("name") == null || e.get("name").isJsonNull() || e.get("name").getAsString().isEmpty()) {
            nameVal = "Empty";
        } else {
            nameVal = e.get("name").getAsString();
        }

        // Handle state
        if (e.get("state") == null || e.get("state").isJsonNull() || e.get("state").getAsString().isEmpty()) {
            stateVal = "Empty";
        } else {
            stateVal = e.get("state").getAsString(); // !! fixed typo: you were reading name here by accident
        }

        // Handle link
        if (e.get("link") == null || e.get("link").isJsonNull() || e.get("link").getAsJsonObject().entrySet().isEmpty()) {
            linkVal = ""; // Just empty string if no link
        } else {
            linkVal = e.get("link").toString(); // Safe serialization to string
        }

        // Handle time
        try {
            System.out.println(e.get("type"));
            time = LocalDateTime.parse(e.get("type").getAsJsonObject()
                    .get("Project").getAsJsonObject()
                    .get("doneBy").getAsString());
            setTimeUntilExp(getDaysBetweenNowAndThen(time));
        } catch (Exception ex) {
            System.err.println("Failed to parse doneBy for UUID " + e.get("uuid") + ": " + e.get("doneBy"));
            ex.printStackTrace();
            setTimeUntilExp("0");
        }

        // Set values
        setName(nameVal);
        setLink(linkVal);
        setState(stateVal);
    }

    private String getDaysBetweenNowAndThen(LocalDateTime time) {
        LocalDateTime doneBy = time;
        LocalDateTime now = LocalDateTime.now();
        return String.valueOf(ChronoUnit.DAYS.between(now, doneBy));
    }

    public void openEntry() {
        appController.loadGrid(uuid);
    }

    public void setName(String name){
        nameLabel.setText(name);
    }
    public void setLink(String link){
        linkLabel.setText(link);
    }
    public void setState(String state){
        stateLabel.setText(state);
    }
    public void setTimeUntilExp(String timeUntilExp){
        timeUntilExpLabel.setText(timeUntilExp);
    }

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    @FXML
    private VBox root;

    @FXML
    private void onMouseEnter() {
        root.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #bbb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 3);");
    }

    @FXML
    private void onMouseExit() {
        root.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);");
    }

}
