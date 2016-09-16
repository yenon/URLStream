import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.*;

/**
 * Created by yenon on 15/09/16.
 */
public class ConnectionHandler extends Thread{

    private Controller controller;
    private String connection,authorisation,type,input;
    private BufferedOutputStream outputStream;

    public ConnectionHandler(Controller controller,String connection,String authorization,String type,String input){
        this.controller=controller;
        this.connection=connection;
        this.authorisation=authorization;
        this.type=type;
        this.input=input;
    }

    public void postData(String input){
        if(outputStream!=null){
            try {
                outputStream.write(input.getBytes());
            } catch (IOException e) {
                showErrorDialog();
                interrupt();
            }
        }
    }

    @Override
    public void run(){
        BufferedReader inputStream=null;
        outputStream=null;
        try{
            URLConnection urlc;
            if(type.equals("")) {
                urlc = new URL(connection).openConnection();
            }else {
                HttpURLConnection httpurlc = (HttpURLConnection) new URL(connection).openConnection();
                httpurlc.setRequestMethod(type.toUpperCase());
                if(!input.equals("")) {
                    httpurlc.setDoOutput(true);
                    OutputStream os = httpurlc.getOutputStream();
                    os.write(input.getBytes());
                    os.close();
                }
                urlc=httpurlc;
            }
            if(!authorisation.equals("")) {
                urlc.setRequestProperty("Authorization",Base64.encode(("Basic "+authorisation).getBytes()));
            }
            inputStream=new BufferedReader(new InputStreamReader(urlc.getInputStream()));
        }catch (Exception ex){
            ex.printStackTrace();
            if(!(ex instanceof MalformedURLException)){
                showErrorDialog(ex.getMessage());
                return;
            }
            connection=connection.replaceAll("[^0-9.:]+","");
            String[] split = connection.split(":");
            if(split.length==2){
                try {
                    Socket socket = new Socket(split[0],Integer.parseInt(split[1]));
                    inputStream=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    outputStream=new BufferedOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(inputStream==null){
            controller.setConnected(false);
            return;
        }
        controller.setInputEnabled(outputStream!=null);
        try {
            String input;
            controller.setConnected(true);
            while(!isInterrupted()&&(input=inputStream.readLine())!=null){
                if(!isInterrupted()) {
                    controller.append(input);
                }
            }
        } catch (IOException e) {
            showErrorDialog();
        }
        controller.setConnected(false);
    }

    private boolean errorShown=false;

    private void showErrorDialog(){
        if(!errorShown){
            errorShown=true;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Connection closed");
                    alert.setContentText("The server unexpectedly closed the connection!");
                    alert.showAndWait();
                }
            });
        }
    }

    private void showErrorDialog(final String msg){
        if(!errorShown){
            errorShown=true;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Exception occured:");
                    alert.setContentText(msg);
                    alert.showAndWait();
                }
            });
        }
    }
}
