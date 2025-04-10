package com.hetlesaetherta.gui_javafx;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.util.*;

public class AppController {
    private final JsonObject exampleJsonEntity = new Gson().fromJson("{" +
            "    \"type\": {" +
            "      \"Project\": {" +
            "        \"doneBy\": \"2025-05-04T23:59:00\"" +
            "      }" +
            "    }," +
            "    \"name\": \"it worked\"," +
            "    \"location\": \"R1\"," +
            "    \"state\": \"Active\"," +
            "    \"link\": {\"key\": \"val\"}," +
            "    \"dmp\": {" +
            "      \"creationTime\": \"2025-04-08T01:17:18.6105987\"," +
            "      \"frontend_dmp\": \"info\"," +
            "      \"backendClient\": \"desktop\"" +
            "    }" +
            "}", JsonObject.class);

    public GridPane grid;
    private final Deque<String> history = new ArrayDeque<>();
    private String currentLevel;

    @FXML
    public Button backButton;
    @FXML
    public Label state;
    @FXML
    public Label name;
    @FXML
    public Label link;
    @FXML
    public Label doneBy;

    @FXML
    public TextField nameField;
    @FXML
    public TextField stateField;
    @FXML
    public TextField doneByField;

    @FXML
    private Label welcomeText;
    @FXML
    private VBox createEntryPane;
    @FXML
    private VBox parentVBox;
    @FXML
    private StackPane rootPane;

    @FXML
    public VBox linkFieldsBox;
    @FXML
    public Button confirmButton;

    private final List<TextField> keyFields = new ArrayList<>();
    private final List<TextField> valueFields = new ArrayList<>();

    Api api = new Api();

    @FXML
    protected void loadGrid(String uuid) {
        if (rootPane != null) {
            rootPane.getChildren().clear();
        }

        GridPane newGrid = new GridPane();
        newGrid.setHgap(10);
        newGrid.setVgap(10);

        JsonObject json = api.fetchAll();
        List<JsonObject> elements = new ArrayList<>();

        JsonObject currentEntity = json.getAsJsonObject(uuid);

        if (currentEntity != null && currentEntity.has("children") && !currentEntity.get("children").isJsonNull()) {
            String[] children = currentEntity.get("children").getAsString().split(";");

            for (String c_id : children) {
                if (json.has(c_id) && !json.get(c_id).isJsonNull()) {
                    elements.add(json.get(c_id).getAsJsonObject());
                }
            }
        }

        try {
            int gridLength = 0;

            for (JsonObject element : elements) {
                if (element.has("state") && element.get("state").isJsonNull()) continue;

                FXMLLoader loader = new FXMLLoader(getClass().getResource("entry.fxml"));
                VBox entry = loader.load();
                EntryController controller = loader.getController();
                controller.constructEntry(element);
                controller.setUuid(element.get("uuid").getAsString());
                controller.addAppController(this);

                int col = gridLength % 3;
                int row = gridLength / 3;
                newGrid.add(entry, col, row);
                gridLength++;
            }

            VBox layoutBox = new VBox();
            layoutBox.setFillWidth(true);

            currentLevel = uuid;
            VBox header = constructHeader(api.fetch(currentLevel));

            header.setMaxWidth(Double.MAX_VALUE);
            layoutBox.getChildren().add(header);

            if (!elements.isEmpty()) {
                layoutBox.getChildren().add(newGrid);
            }

            layoutBox.setAlignment(Pos.TOP_CENTER);
            layoutBox.setSpacing(20);
            rootPane.getChildren().add(layoutBox);

            newGrid.setAlignment(Pos.TOP_CENTER);

            killCreateUpdateScreen();
            System.out.println(history);
            System.out.println(currentLevel);
            pushToHistory(uuid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pushToHistory(String uuid) {
        if (history.contains(uuid)) {
            while (!history.isEmpty()) {
                String popped = history.pop();
                if (popped.equals(uuid)) {
                    break;
                }
            }
        }
        history.push(uuid);
    }

    @FXML
    private VBox constructHeader(JsonObject json) {
        try {

            json = api.fetch(currentLevel).getAsJsonObject(currentLevel);
            System.out.println("yayayay"  + json);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("header.fxml"));
            loader.setController(this);
            VBox entry = loader.load();

            backButton.setVisible(!"0".equals(currentLevel));

            Label stateLabel = (Label) entry.lookup("#state");
            Label nameLabel = (Label) entry.lookup("#name");
            VBox linkBox = (VBox) entry.lookup("#linkBox");
            Label doneByLabel = (Label) entry.lookup("#doneBy");


            if (stateLabel != null) {
                stateLabel.setText(json.get("state").getAsString());
            }
            if (nameLabel != null) {
                nameLabel.setText(json.get("name").getAsString());
            }
            if (linkBox != null) {
                linkBox.getChildren().clear(); // clear old links first
                if (json.has("link") && json.get("link").isJsonObject()) {
                    JsonObject links = json.getAsJsonObject("link");
                    for (Map.Entry<String, JsonElement> e : links.entrySet()) {
                        Label linkEntry = new Label(e.getKey() + ": " + e.getValue().getAsString());
                        linkEntry.setWrapText(true);
                        linkEntry.setStyle("-fx-text-fill: #555; -fx-font-size: 12;");
                        linkBox.getChildren().add(linkEntry);
                    }
                }
            }
            if (doneByLabel != null) {
                JsonObject type = json.get("type").getAsJsonObject();
                System.out.println(type);
                if (type.has("Project")) {
                    JsonObject project = type.getAsJsonObject("Project");
                    System.out.println(project);
                    if (project.has("doneBy")) {
                        System.out.println(project.get("doneBy").getAsString());
                        doneByLabel.setText(project.get("doneBy").getAsString());
                    }
                }
            }
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    @FXML
    protected void createNewEntry() {
        try {
            String name = nameField.getText();
            String state = stateField.getText();
            String doneBy = doneByField.getText();
            JsonObject links = collectLinks();

            JsonObject entity = new JsonObject();

            // Build type -> Project -> doneBy (STRING, not object)
            JsonObject type = new JsonObject();
            JsonObject project = new JsonObject();
            project.addProperty("doneBy", doneBy);
            type.add("Project", project);
            entity.add("type", type);

            // Set other flat fields
            entity.addProperty("name", name);
            entity.addProperty("state", state);
            entity.add("link", links);

            // --- add empty dmp ---
            entity.add("dmp", new JsonObject());

            System.out.println(entity);
            if (api.create(currentLevel, entity) != null) {
                System.out.println("Created successfully!");
                loadGrid(currentLevel); // reload to show new entry
            } else {
                System.out.println("Create failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private JsonObject constructNewEntity(String name, String state, JsonObject links, String doneBy) {
        JsonObject entity = new JsonObject();

        JsonObject type = new JsonObject();
        JsonObject project = new JsonObject();
        project.addProperty("doneBy", doneBy);
        type.add("Project", project);
        entity.add("type", type);

        entity.addProperty("name", name);
        entity.addProperty("state", state);

        entity.add("link", links);
        entity.addProperty("depth", 0);
        entity.addProperty("children", "");

        return entity;
    }

    private JsonObject collectLinks() {
        JsonObject links = new JsonObject();
        for (int i = 0; i < Math.min(keyFields.size(), valueFields.size()); i++) {
            String key = keyFields.get(i).getText().trim();
            String val = valueFields.get(i).getText().trim();
            if (!key.isEmpty() && !val.isEmpty()) {
                links.addProperty(key, val);
            }
        }
        return links;
    }

    @FXML
    protected void fillTextFields() {
        Random random = new Random();

        nameField.setText("Test Name " + (random.nextInt(90) + 10));
        stateField.setText("Active");
        doneByField.setText("2025-12-" + (random.nextInt(20) + 10) + "T23:59:00");

        for (int i = 0; i < keyFields.size(); i++) {
            keyFields.get(i).setText("key" + (i + 1));
            valueFields.get(i).setText("value" + (random.nextInt(900) + 100));
        }
    }

    @FXML
    protected void addLinkField() {
        HBox linkRow = new HBox(10); // Horizontal layout with spacing 10px
        linkRow.setAlignment(Pos.CENTER_LEFT);

        TextField keyField = new TextField();
        keyField.setPromptText("Key");
        keyField.setPrefWidth(150); // Adjust width if needed

        TextField valueField = new TextField();
        valueField.setPromptText("Value");
        valueField.setPrefWidth(150); // Adjust width if needed

        Button removeButton = new Button("❌");
        removeButton.setOnAction(e -> {
            int index = linkFieldsBox.getChildren().indexOf(linkRow);
            if (index >= 0) {
                linkFieldsBox.getChildren().remove(linkRow);
                keyFields.remove(index);
                valueFields.remove(index);
            }
        });

        keyFields.add(keyField);
        valueFields.add(valueField);

        linkRow.getChildren().addAll(keyField, valueField, removeButton);
        linkFieldsBox.getChildren().add(linkRow);
    }

    public void start() {
        loadGrid("0");
    }

    @FXML
    public CheckBox recursiveDelete;
    @FXML
    protected void deleteEntry() {
        String uuidToDelete = currentLevel;
        boolean recursive = recursiveDelete.isSelected();

        if (api.delete(uuidToDelete, recursive)) {
            // Instead of goBack() immediately...
            if (!history.isEmpty()) {
                history.pop(); // remove current deleted node
            }
            if (!history.isEmpty()) {
                String parentUuid = history.peek();
                loadGrid(parentUuid); // reload parent properly from fresh server data
            } else {
                loadGrid("0"); // fallback to root if no parent
            }
        }
    }

    @FXML
    private TextField firstKeyField;

    @FXML
    private TextField firstValueField;

    @FXML
    protected void spawnCreateScreen() {
        createEntryPane.setVisible(true);
        createEntryPane.setManaged(true);

        keyFields.clear();
        valueFields.clear();
        keyFields.add(firstKeyField);
        valueFields.add(firstValueField);

        confirmButton.setText("Create");
        confirmButton.setOnAction(e -> createNewEntry());
    }

    @FXML
    protected void spawnUpdateScreen() {
        createEntryPane.setVisible(true);
        createEntryPane.setManaged(true);

        confirmButton.setText("Update");
        confirmButton.setOnAction(e -> updateEntry());
    }

    protected void updateEntry() {
        try {
            String name = nameField.getText();
            String state = stateField.getText();
            String doneBy = doneByField.getText();
            JsonObject links = collectLinks();

            JsonObject entity = new JsonObject();

            // Build type -> Project -> doneBy
            JsonObject type = new JsonObject();
            JsonObject project = new JsonObject();
            project.addProperty("doneBy", doneBy);
            type.add("Project", project);
            entity.add("type", type);

            // Set fields
            entity.addProperty("name", name);
            entity.addProperty("state", state);
            entity.add("link", links);

            JsonObject existing = api.fetch(currentLevel).get(currentLevel).getAsJsonObject();

            if (existing != null) {
                if (existing.has("dmp")) {
                    entity.add("dmp", existing.get("dmp"));
                } else {
                    entity.add("dmp", new JsonObject());
                }

            } else {
                // fallback in case fetch fails
                entity.add("dmp", new JsonObject());
            }

            System.out.println(entity);

            if (api.update(currentLevel, entity) != null) {
                System.out.println("Updated successfully!");
                loadGrid(currentLevel); // reload to show updated info
            } else {
                System.out.println("Update failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void killCreateUpdateScreen() {
        createEntryPane.setVisible(false);
        createEntryPane.setManaged(false);
    }

    @FXML
    protected void goBack() {
        if (history.size() > 1) {
            history.pop(); // remove current
            String previousUuid = history.pop(); // go back to previous
            loadGrid(previousUuid);
        }
    }
}
