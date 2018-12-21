package View;

import Controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class SearchResults  extends View{

    Controller controller;
    Stage primaryStage;
    TreeMap<Integer, Vector<String>> result;
    public ListView<String> resultList;


    public void setController(Controller controller, Stage primaryStage) {
        this.controller = controller;
        this.primaryStage = primaryStage;
        this.result = View.result;
        setResult();

    }

    private void setResult() {
        for(Map.Entry<Integer,Vector<String>> entry: View.result.entrySet()){
            resultList.getItems().add("result for: " + entry.getKey());
            ObservableList<String> dictionaryObservable = FXCollections.observableArrayList(entry.getValue());
            for(String str : dictionaryObservable)
                resultList.getItems().add(str);

        }
    }

}
