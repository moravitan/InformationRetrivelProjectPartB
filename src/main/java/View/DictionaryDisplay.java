package View;

import Controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.TreeSet;

/**
 * represent the stage on the dictionary display
 */
public class DictionaryDisplay extends View {


    public ListView<String> lv_dictionaryDisplay;
    private Controller controller;
    private Stage stage;

    public void setController(Controller controller, Stage stage) {
        this.controller = controller;
        this.stage = stage;
        setText();
    }

    /**
     * sets the dictionary content in the right place in the stage
     */
    public void setText(){
        TreeSet<String> dictionary = controller.dictionaryToString();
        ObservableList<String> dictionaryObservable = FXCollections.observableArrayList(dictionary);
        for(String str : dictionaryObservable )
            lv_dictionaryDisplay.getItems().add(str);

    }
}
