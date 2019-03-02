package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.openmuc.j60870.*;
import org.openmuc.j60870.app.ConsoleClient;

import java.io.IOException;

public class Controller {

    InformationElement[][] informationElements;
    InformationObject informationObject;
    InformationObject[] informationObjects;
    ASdu testTI;
    Connection connectionClient;
    ConnectionEventListener connectionEventListener;
    IecServer iecServer;
    @FXML
    private TextField inputValue;
    @FXML
    private TextArea message;


    //-----------------------------------------------------------------
//-----------------------------------------------------------------
    public void onConnectClient() throws IOException {

        ConsoleClient consoleClient = new ConsoleClient();
        consoleClient.main(new String[]{"127.0.0.1 2404 1"});
    }

    //-----------------------------------------------------------------
//-----------------------------------------------------------------
    public void disconnectClient() {
        connectionClient.close();
    }

    //-----------------------------------------------------------------
//-----------------------------------------------------------------
    public boolean sendASdu() {
        if (iecServer != null) {
            try {
                float val = Float.valueOf(inputValue.getText());
                iecServer.sendTestTI(val);
            } catch (Exception e) {
                message.appendText("Введено не число\n");
                return false;
            }
            return true;
        }
        message.appendText("Server не создан\n");
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
}



