package View;

import Controller.Controller;
import Model.Engine;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class SearchResults  extends View{

    Controller controller;
    Stage primaryStage;
    TreeMap<Integer, Vector<String>> result;
    public ListView<String> resultList;
    public ListView<String> entites;
    public ChoiceBox queryId;
    public TextField pathToSaveQueryResults;


    public void setController(Controller controller, Stage primaryStage) {
        this.controller = controller;
        this.primaryStage = primaryStage;
        this.result = View.result;
        setResult();
        setQueryId();

    }

    private void setQueryId(){
        for(Map.Entry<Integer,Vector<String>> entry: View.result.entrySet()){
            queryId.getItems().add(entry.getKey());
        }
        queryId.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                Integer key = (Integer) queryId.getItems().get((Integer) number2);
                Vector <String> result = View.result.get(key);
                ObservableList<String> dictionaryObservable = FXCollections.observableArrayList(result);
                for(String str : dictionaryObservable) {
                    resultList.getItems().add(str);
                }
            }
        });
        resultList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                this.entites.getItems().clear();
                ObservableList<String> entites = FXCollections.observableArrayList(Engine.mapOfDocs.get((String)newSelection).getTopFiveEntities().keySet());
                for(String str : entites) {
                    this.entites.getItems().add(str);
                }
            }
        });
    }

    private void setResult() {
        int totalCounter = 0;
        int counter = 0;
        for(Map.Entry<Integer,Vector<String>> entry: View.result.entrySet()){
            HashSet<String> resules = new HashSet<>();
            try {
                BufferedReader bf = new BufferedReader(new FileReader("C:\\Users\\איתן אביטן\\Downloads\\לימודים\\אחזור מידע\\פרויקט מנוע חיפוש\\spliter qrels\\" + entry.getKey() + ".txt"));
                String line = bf.readLine();
                while (line != null){
                    resules.add(line);
                    line = bf.readLine();
                }
                bf.close();
                //resultList.getItems().add("result for: " + entry.getKey());
                ObservableList<String> dictionaryObservable = FXCollections.observableArrayList(entry.getValue());
                for(String str : dictionaryObservable) {
                    if (resules.contains(str)) {
                        counter++;
                        totalCounter++;
                    }
                }
                System.out.println("Number of matches for query: " + entry.getKey() + " " + counter) ;
                counter = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Number of total matches : " + totalCounter);

    }

    public void loadPath(){
        try {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Open Resource File");
            File selectedFile = fileChooser.showDialog(new Stage());
            if (selectedFile != null) {
                pathToSaveQueryResults.setText(selectedFile.getAbsolutePath());
            }
        }catch (Exception e){
            e.getStackTrace();
        }

    }

    public void saveQueryResultToFile(){
        if (pathToSaveQueryResults.getText() == null || pathToSaveQueryResults.getText().trim().isEmpty())
            alert("You did not enter any path", Alert.AlertType.ERROR);
        else {
            controller.saveQueryResultToFile(pathToSaveQueryResults.getText());
            alert("Result saved to file", Alert.AlertType.INFORMATION);
        }
    }

}
