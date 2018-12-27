package View;

import Controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.BufferedReader;
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


    public void setController(Controller controller, Stage primaryStage) {
        this.controller = controller;
        this.primaryStage = primaryStage;
        this.result = View.result;
        setResult();

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
                resultList.getItems().add("result for: " + entry.getKey());
                ObservableList<String> dictionaryObservable = FXCollections.observableArrayList(entry.getValue());
                for(String str : dictionaryObservable) {
                    resultList.getItems().add(str);
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
        System.out.println("Number of total matches : " + totalCounter) ;

    }

}
