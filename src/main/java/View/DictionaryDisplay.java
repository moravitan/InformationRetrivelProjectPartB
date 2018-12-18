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
/*        //if(controller.dictionaryToString()!=null) {
//        TreeSet <String> dictionary = controller.dictionaryToString();
//        ObservableList<String> dictionaryObservable = FXCollections.observableArrayList(dictionary);
        StringBuilder ans = new StringBuilder("");
        try{
            File dictionaryFile = new File(Indexer.pathToSaveIndex + "\\dictionaryToDisplay.txt");
            BufferedReader bf = new BufferedReader(new FileReader(dictionaryFile));
            String line = bf.readLine();
            while (line != null){
                //lv_dictionaryDisplay.getItems().add(line.substring(0,details[0]) + details[1] + "\n");
                lv_dictionaryDisplay.getItems().add(line);
                line = bf.readLine();
            }
            bf.close();
        } catch (IOException e) { }
        //lv_dictionaryDisplay.setItems(dictionaryObservable);
            //setText(controller.dictionaryToString());
        //}*/
        TreeSet<String> dictionary = controller.dictionaryToString();
        ObservableList<String> dictionaryObservable = FXCollections.observableArrayList(dictionary);
        for(String str : dictionaryObservable )
            lv_dictionaryDisplay.getItems().add(str);

    }
}
