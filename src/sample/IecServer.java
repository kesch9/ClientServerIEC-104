package sample;

import org.openmuc.j60870.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class IecServer {
    private ServerEventListener serverEventListener;
    private Server server;
    private Boolean serverIsStarted = false;
    private int ASDU = 1;

    //Реализация Serverlistener
    public class ServerListener implements ServerEventListener {

        public class ConnectionListener implements ConnectionEventListener {

            private final Connection connection;
            private final int connectionId;

            public ConnectionListener(Connection connection, int connectionId) {
                this.connection = connection;
                this.connectionId = connectionId;
            }

            @Override
            public void newASdu(ASdu aSdu) {
                try {

                    switch (aSdu.getTypeIdentification()) {
                        // interrogation command
                        case C_IC_NA_1:
                            connection.sendConfirmation(aSdu);
                            System.out.println("Got interrogation command. Will send scaled measured values.\n");
                            connection.send(new ASdu(TypeId.M_ME_NB_1, true, CauseOfTransmission.SPONTANEOUS, false, false,
                                    0, ASDU,
                                    new InformationObject[] { new InformationObject(1, new InformationElement[][] {
                                            { new IeScaledValue(-32768), new IeQuality(true, true, true, true, true) },
                                            { new IeScaledValue(10), new IeQuality(true, true, true, true, true) },
                                            { new IeScaledValue(-5), new IeQuality(true, true, true, true, true) } }) }));

                            break;
                        default:
                            System.out.println("Got unknown request: " + aSdu + ". Will not confirm it.\n");
                    }

                } catch (EOFException e) {
                    System.out.println("Will quit listening for commands on connection (" + connectionId
                            + ") because socket was closed.");
                } catch (IOException e) {
                    System.out.println("Will quit listening for commands on connection (" + connectionId
                            + ") because of error: \"" + e.getMessage() + "\".");
                }

            }

            @Override
            public void connectionClosed(IOException e) {
                System.out.println("Connection (" + connectionId + ") was closed. " + e.getMessage());
                connections.remove(connection);
            }

        }

        @Override
        public void connectionIndication(Connection connection) {

            int myConnectionId = connectionIdCounter++;
            System.out.println("A client has connected using TCP/IP. Will listen for a StartDT request. Connection ID: "
                    + myConnectionId);

            try {
                connection.waitForStartDT(new ServerListener.ConnectionListener(connection, myConnectionId), 5000);
                connections.add(connection);
            } catch (IOException e) {
                System.out.println("Connection (" + myConnectionId + ") interrupted while waiting for StartDT: "
                        + e.getMessage() + ". Will quit.");
                return;
            } catch (TimeoutException e) {
            }

            System.out.println(
                    "Started data transfer on connection (" + myConnectionId + ") Will listen for incoming commands.");

        }

        @Override
        public void serverStoppedListeningIndication(IOException e) {
            System.out.println(
                    "Server has stopped listening for new connections : \"" + e.getMessage() + "\". Will quit.");
        }

        @Override
        public void connectionAttemptFailed(IOException e) {
            System.out.println("Connection attempt failed: " + e.getMessage());

        }

    }
    private int connectionIdCounter = 1;
    private List <Connection> connections = new ArrayList<>(100);

    public IecServer() {
        serverEventListener = new ServerListener();
        server = new Server.Builder().build();
    }


    public void setASDU(int ASDU) {
        this.ASDU = ASDU;
    }


    public boolean startServer(){
        try {
            server.start(serverEventListener);
            serverIsStarted = true;
            System.out.println("Server is start");
        } catch (IOException e) {
            System.out.println("Unable to start listening: \"" + e.getMessage() + "\". Will quit.");
            return false;
        }
        return true;
    }

    public void stopServer(){
        if(serverIsStarted){
            server.stop();
            serverIsStarted = false;
        }
    }

    public boolean serverIsStarted(){
        return serverIsStarted;
    }

    public void sendTestTI(float value) {
        if (!connections.isEmpty()) {
            System.out.println("Отправим данные");
            for (Connection connection : connections) {
                try {
                    connection.send(new ASdu(TypeId.M_ME_TF_1, false, CauseOfTransmission.SPONTANEOUS, false, false,
                            0, ASDU,
                            new InformationObject[]{new InformationObject(1, new InformationElement[][]{
                                    { new IeShortFloat(value),
                                            new IeQuality(true, true, true, true, true),
                                            new IeTime56(System.currentTimeMillis())}})}));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

