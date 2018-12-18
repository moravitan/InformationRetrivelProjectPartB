package View;

import Controller.Controller;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.util.Optional;

public class OperatingWindow extends View{
    public javafx.scene.control.Button btn_browseCorpus;
    public javafx.scene.control.Button btn_browseIndexDestination;
    public javafx.scene.control.Button btn_DisplayDictionary;
    public javafx.scene.control.Button btn_UploadDictToMem;
    public javafx.scene.control.Button btn_resetAll;
    public javafx.scene.control.CheckBox cb_stemming;
    public javafx.scene.control.ComboBox cb_languageSelect;
    public javafx.scene.control.TextField tf_browseCorpus;
    public javafx.scene.control.TextField tf_browseIndexDestination;
    private String CorpusPath;
    private String indexSavingPath;
    private Controller controller;
    private Stage stage;
    private DictionaryDisplay dictionaryDisplay;


    public void setController(Controller controller, Stage stage) {
        this.controller = controller;
        this.stage = stage;
    }


    /**
     * load path from DirectoryChooser to the textField represents the path
     * for pathOfCorpus and for pathToSave
     * @param actionEvent - clicking on one of the two DirectoryChooser
     */
    public void loadPath(ActionEvent actionEvent){
        try
        {
            JButton b = new JButton();
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Directory");
            File file = chooser.showDialog(new Stage());
            if (file != null) {
                Button sourceButton = (Button) actionEvent.getSource();
                String btnID = sourceButton.getId();
                if (btnID.equals("btn_browseCorpus")) {
                    tf_browseCorpus.setText(file.getAbsolutePath());
                }
                if (btnID.equals("btn_browseIndexDestination")) {
                    tf_browseIndexDestination.setText(file.getAbsolutePath());
                }
            }
        }
        catch(
                IllegalArgumentException e)
        {
            throw e;
        }
    }

    /**
     * creates the index using the link of the controller to the model
     * @param actionEvent clicking on the "Create Index" button
     */
    public void createIndex(ActionEvent actionEvent){

        if(tf_browseCorpus == null || tf_browseIndexDestination == null || tf_browseCorpus.getText().trim().isEmpty() || tf_browseIndexDestination.getText().trim().isEmpty()) {
            alert("One required Path or more is empty", Alert.AlertType.INFORMATION);
            //tf_browseCorpus.setDisable(false);
            //tf_browseIndexDestination.setDisable(false);
        } else{
            controller.loadPath(tf_browseCorpus.getText(), tf_browseIndexDestination.getText(), cb_stemming.isSelected());
            if (Controller.isValid) {
                String massage = "Number of documents have been indexed: " + controller.getNumberOfDocs() + "\n" + "Number of terms: " + controller.getNumberOfTerms() + "\n" + "Total time of creating index (seconds): " + controller.getTotalTimeToIndex();
                alert(massage, Alert.AlertType.INFORMATION);
                btn_DisplayDictionary.setDisable(false);
                btn_UploadDictToMem.setDisable(false);
                btn_resetAll.setDisable(false);
                ObservableList<String> languages = controller.getDocumentsLanguages();
                //insert documents languages into the combo box
                cb_languageSelect.setDisable(false);
                cb_languageSelect.setItems(languages);
                cb_languageSelect.setVisibleRowCount(languages.size() / 2);
                Controller.isValid = false;
            }
        }
    }

    /**
     * makes sure the user really want to reset all memory of the program including main memory and the saved data in disk
     * @param actionEvent clicking on "Reset All" button
     */
    public void resetValidation(ActionEvent actionEvent){
        //alert("Are you sure you want to reset all memory?", Alert.AlertType.CONFIRMATION);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
        alert.setContentText("Are you sure you want to reset all memory?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // ... user chose OK
            controller.resetAll();
            alert("All memory have been reset", Alert.AlertType.INFORMATION);
            tf_browseCorpus.setDisable(false);
            tf_browseIndexDestination.setDisable(false);
            btn_resetAll.setDisable(true);
            btn_DisplayDictionary.setDisable(true);
            btn_UploadDictToMem.setDisable(true);
            cb_languageSelect.setDisable(true);

        }else
            alert.close();
    }

    /**
     * displays the dictionary has been created while creating the index
     * display in a new stage (window)
     * @param actionEvent - clicking on "Display Dictionary" button
     */
    public void displayDictionary(ActionEvent actionEvent){
        newStage("DictionaryDisplay.fxml", "", dictionaryDisplay, 350, 560, controller);
//        dictionaryDisplay.setText();
    }

    public void uploadDictionaryToMem(ActionEvent actionEvent){
        controller.uploadDictionaryToMem();
        alert("The dictionary have been uploaded to memory." , Alert.AlertType.INFORMATION);
    }
}
