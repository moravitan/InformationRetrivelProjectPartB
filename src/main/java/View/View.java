package View;

import Controller.Controller;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.*;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;


public class View implements Observer {
    private Controller controller;
    private Stage primaryStage;
    public javafx.scene.control.Button btn_start;
    public javafx.scene.control.Button btn_loadQuery;
    public javafx.scene.control.Button btn_loadIndexPath;
    public javafx.scene.control.Button btn_searchSingleQuery;
    public javafx.scene.control.Button btn_searchMultiQueries;
    public javafx.scene.control.TextField pathToQueriesFile;
    public javafx.scene.control.TextField pathToIndexDirectory;
    public javafx.scene.control.TextField singleQuery;
    public javafx.scene.control.CheckBox cb_stemming;
    public javafx.scene.layout.VBox vb_cities;
    public ImageView logo;
    private OperatingWindow operatingWindow;
    private SearchResults searchResults;
    private HashSet<String> cities;
    public static TreeMap<Integer, Vector<String>> result;

    @Override
    public void update(Observable o, Object arg) {

    }

    public void setController(Controller controller, Stage primaryStage) {
        this.controller = controller;
        this.primaryStage = primaryStage;
        this.cities = new HashSet<>();
        btn_searchSingleQuery.setDisable(true);
        btn_searchMultiQueries.setDisable(true);
        btn_loadQuery.setDisable(true);
        Image img2 = null;
        try {
            img2 = new Image(getClass().getResource("/logo.png").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        logo.setImage(img2);
    }

    /**
     * opens the main operations window
     * @param actionEvent - pressing "Start" button
     */
    public void Start(ActionEvent actionEvent) {
        newStage("OperatingWindow.fxml", "", operatingWindow, 670, 490, controller);
    }

    /**
     * handle mouseClick on the close window button
     * shows an alert asking the user if he really want to close, if yes, closing the window
     * @param primaryStage - the stage being closed
     */
    protected void SetStageCloseEvent(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Back");
                alert.setContentText("Are you sure you want to exit?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    // ... user chose OK
                    // Close program
                    //enable start button when return to primary stage (home window)
                    if(btn_start!=null)
                        btn_start.setDisable(false);
                } else {
                    // ... user chose CANCEL or closed the dialog
                    windowEvent.consume();

                }
            }
        });
    }

    /**
     *
     * @param actionEvent
     */
    public void searchQuery(ActionEvent actionEvent) {
        if (singleQuery.getText() == null || singleQuery.getText().trim().isEmpty())
            alert("You did not enter any query", Alert.AlertType.ERROR);
        else {
            controller.setIsStemming(cb_stemming.isSelected());
            result = controller.processQuery(singleQuery.getText(), cities);
            if (result.size() == 0)
                alert("Sorry, but we couldn't find results for your search", Alert.AlertType.INFORMATION);
            else
                newStage("SearchResults.fxml", "", searchResults, 281, 400, controller);
        }
    }

    /**
     *
     * @param actionEvent
     */
    public void searchQueryFile(ActionEvent actionEvent) {
        if (pathToQueriesFile.getText() == null || pathToQueriesFile.getText().trim().isEmpty())
            alert("You did not enter any path", Alert.AlertType.ERROR);
        else {
            result = controller.processQueryFile(new File(pathToQueriesFile.getText()), cities);
            if (result.size() == 0)
                alert("Sorry, but we couldn't find results for your search", Alert.AlertType.INFORMATION);
            else
                newStage("SearchResults.fxml", "", searchResults, 281, 400, controller);

        }
    }

    public void loadQueryPath(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile != null) {
                pathToQueriesFile.setText(selectedFile.getAbsolutePath());
            }
        }catch (Exception e){
            e.getStackTrace();
        }
    }

    public void loadIndexPath(ActionEvent actionEvent) {
        try {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Open Resource File");
            File selectedFile = fileChooser.showDialog(new Stage());
            if (selectedFile != null) {
                pathToIndexDirectory.setText(selectedFile.getAbsolutePath());
            }
        }catch (Exception e){
            e.getStackTrace();
        }
    }

    /**
     * This method load the dictionary file from the pathToIndexDirectory directory into a hash map
     * @param actionEvent
     */
    public void loadDictionary(ActionEvent actionEvent) {
        if (pathToIndexDirectory.getText().length() > 0) {
            controller.setIsStemming(cb_stemming.isSelected());
            controller.setPathToSaveIndex(pathToIndexDirectory.getText());
            controller.uploadDictionaryToMem();
            alert("The dictionary have been uploaded to memory.", Alert.AlertType.INFORMATION);
            btn_loadQuery.setDisable(false);
            btn_searchSingleQuery.setDisable(false);
            btn_searchMultiQueries.setDisable(false);
            // set corpus cities
            ObservableList<String> corpusCities = controller.readDocumentsLanguages();
            org.controlsfx.control.CheckComboBox<String> checkComboBox = new CheckComboBox<String>(corpusCities);
            vb_cities.getChildren().clear();
            vb_cities.getChildren().addAll(checkComboBox);
            // when a city has been checked add it to hash set
            checkComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
                public void onChanged(ListChangeListener.Change<? extends String> c) {
                    cities.add(checkComboBox.getCheckModel().getCheckedItems().toString());
                }
            });
        }
        else{
            alert("Please enter a path to your index directory", Alert.AlertType.ERROR);

        }
    }

    /**
     * creates a new window, based on given details and shows it
     * @param fxmlName - name of the stage fxml file
     * @param title - title of the window
     * @param windowName - name of the java class represents the stage
     * @param width of window
     * @param height of window
     * @param controller - controller of the program, link between the view and model
     */
    protected void newStage(String fxmlName,String title, View windowName, int width, int height, Controller controller){
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = null;
        try {
            root = fxmlLoader.load(getClass().getResource("/" + fxmlName).openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setResizable(false);
        SetStageCloseEvent(stage);
        stage.show();
        windowName = fxmlLoader.getController();
        windowName.setController(controller, stage);
        controller.addObserver(windowName);
    }

    /**
     * creates an alert, the type is on request
     * @param messageText the massage being shown in the alert
     * @param alertType -Alert.AlertType (Information, Confirmation... )
     */
    protected void alert(String messageText, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setContentText(messageText);
        alert.showAndWait();
        alert.close();

    }

}
