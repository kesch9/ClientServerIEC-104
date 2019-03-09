package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.openmuc.j60870.*;

import java.io.IOException;

public class Controller {

    InformationElement[][] informationElements;
    InformationObject informationObject;
    InformationObject[] informationObjects;
    ASdu testTI;

    IecServer iecServer;
    IecClient iecClient;
    @FXML
    private TextField inputValue;
    @FXML
    private TextArea message;


    public void initialize(){
    }


    //-----------------------------------------------------------------
//-----------------------------------------------------------------
    public void onConnectClient() throws IOException {
        iecClient = new IecClient();
        iecClient.startClient();
    }

    //-----------------------------------------------------------------
//-----------------------------------------------------------------
    public void disconnectClient() {
        if (iecClient != null && iecClient.isClientIsStarted()) {
            iecClient.stopClient();
            System.out.println("Client stop");
        } else {
            System.out.println("Client not create or not start");
        }
    }

    //-----------------------------------------------------------------
//-----------------------------------------------------------------
    public boolean sendASdu() {
        if (iecServer != null) {
            try {
                float val = Float.valueOf(inputValue.getText());
                iecServer.sendTestTI(val);
            } catch (Exception e) {
                message.appendText("Input text is not number\n");
                return false;
            }
            return true;
        }
        message.appendText("Server is not create\n");
        return false;
    }


    //-----------------------------------------------------------------
//-----------------------------------------------------------------
    public void onConnectServer() {
        iecServer = new IecServer();
        iecServer.startServer();
    }

    //-----------------------------------------------------------------
//-----------------------------------------------------------------
    public void disconnectServer() {
        if (iecServer != null && iecServer.serverIsStarted()) {
            iecServer.stopServer();
            System.out.println("Server stop");
        } else {
            System.out.println("Server not create or not start");
        }

    }


    public void sendOBD(ActionEvent actionEvent) {
        if (iecClient != null && iecClient.isClientIsStarted()) {
            iecClient.sendOBD();
            System.out.println("Client send OBD");
        } else {
            System.out.println("Client not create or not start");
        }
    }
}



