package View;

import Controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;


public class View implements Observer {
    private Controller controller;
    private Stage primaryStage;
    public javafx.scene.control.Button btn_start;
    private OperatingWindow operatingWindow;

    @Override
    public void update(Observable o, Object arg) {

    }

    public void setController(Controller controller, Stage primaryStage) {
        this.controller = controller;
        this.primaryStage = primaryStage;
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
