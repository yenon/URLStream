import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by yenon on 15/09/16.
 */
public class Controller {

    private ConnectionHandler connectionHandler;

    @FXML
    GridPane parentPane;

    @FXML
    private TextField textFieldInput,textFieldUrl,textFieldAuth,textFieldType;

    @FXML
    private TextArea textAreaOutput,textAreaInput;

    @FXML
    private Button buttonConnect;

    public void setStage(Stage stage){

    }

    @FXML
    private void initialize(){
        parentPane.setOnDragOver(new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if(db.hasFiles()){
                    event.acceptTransferModes(TransferMode.ANY);
                }
                event.consume();
            }
        });
        parentPane.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if(db.hasFiles()&&db.getFiles().size()==1){
                    Path filePath = db.getFiles().get(0).toPath();
                    if(Files.isRegularFile(filePath)) {
                        try {
                            JSONObject input = new JSONObject(new JSONTokener(Files.newInputStream(filePath)));
                            if (input.has("url")) {
                                textFieldUrl.setText(input.getString("url"));
                            }
                            if (input.has("auth")) {
                                textFieldAuth.setText(input.getString("auth"));
                            }
                            if (input.has("type")) {
                                textFieldType.setText(input.getString("type"));
                            }
                            if (input.has("input")) {
                                textAreaInput.setText(input.getString("input"));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void setInputEnabled(boolean enabled){
        textFieldInput.setDisable(!enabled);
    }

    public void setConnected(boolean connected){
        if(connected){
            buttonConnect.setStyle("-fx-base: #008800;");
        }else {
            buttonConnect.setStyle("-fx-base: #880000;");
        }
    }

    public void append(final String input){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textAreaOutput.appendText(input);
                textAreaOutput.appendText("\n");
            }
        });
    }

    @FXML
    private void onConnect(){
        textAreaOutput.setText("");
        if(connectionHandler!=null){
            connectionHandler.interrupt();
        }
        connectionHandler = new ConnectionHandler(this,textFieldUrl.getText(),textFieldAuth.getText(),textFieldType.getText(),textFieldInput.getText());
        connectionHandler.start();
    }

    @FXML
    private void onInput(){
        if(connectionHandler!=null){
            connectionHandler.postData(textFieldInput.getText());
            textFieldInput.setText("");
        }
    }
}
