package sample;

import org.openmuc.j60870.*;
import org.openmuc.j60870.internal.cli.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class IecClient {

    private boolean clientIsStarted = false;

    private static final StringCliParameter hostParam = new CliParameterBuilder("-h")
            .setDescription("The IP/domain address of the server you want to access.")
            .setMandatory()
            .buildStringParameter("host");
    private static final IntCliParameter portParam = new CliParameterBuilder("-p")
            .setDescription("The port to connect to.")
            .buildIntParameter("port", 2404);
    private static final IntCliParameter commonAddrParam = new CliParameterBuilder("-ca")
            .setDescription("The address of the target station or the broad cast address.")
            .buildIntParameter("common_address", 1);

    private static volatile Connection connection;


    private static class ClientEventListener implements ConnectionEventListener {

        @Override
        public void newASdu(ASdu aSdu) {
            System.out.println("\nReceived ASDU:\n" + aSdu);

        }

        @Override
        public void connectionClosed(IOException e) {
            System.out.print("Received connection closed signal. Reason: ");
            if (!e.getMessage().isEmpty()) {
                System.out.println(e.getMessage());
            }
            else {
                System.out.println("unknown");
            }
        }

    }

    public void startClient() {

        List<CliParameter> cliParameters = new ArrayList<>();
        cliParameters.add(hostParam);
        cliParameters.add(portParam);
        cliParameters.add(commonAddrParam);

        CliParser cliParser = new CliParser("j60870-console-client",
                "A client/master application to access IEC 60870-5-104 servers/slaves.");
        cliParser.addParameters(cliParameters);

        InetAddress address;
        try {
            address = InetAddress.getByName(hostParam.getValue());
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + hostParam.getValue());
            return;
        }

        ClientConnectionBuilder clientConnectionBuilder = new ClientConnectionBuilder(address)
                .setPort(portParam.getValue());

        try {
            connection = clientConnectionBuilder.connect();
        } catch (IOException e) {
            System.out.println("Unable to connect to remote host: " + hostParam.getValue() + ".");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                connection.close();
            }
        });

        try {
            connection.startDataTransfer(new IecClient.ClientEventListener(), 5000);
        } catch (TimeoutException e2) {
            System.out.println("Starting data transfer timed out. Closing connection.");
            connection.close();
            return;
        } catch (IOException e) {
            System.out.println("Connection closed for the following reason: " + e.getMessage());
            return;
        }
        clientIsStarted = true;
        System.out.println("successfully connected");

    }

    public void stopClient() {
        if (clientIsStarted) {
            connection.close();
            clientIsStarted = false;
        }
    }

    public boolean isClientIsStarted () {
        return clientIsStarted;
    }

    public void sendOBD(){
        try {
            byte [] bytes = {0,0,0};
//            public ASdu(TypeId typeId, boolean isSequenceOfElements, int sequenceLength,
//            CauseOfTransmission causeOfTransmission, boolean test, boolean negativeConfirm, int originatorAddress,
//            int commonAddress, byte[] privateInformation)
            connection.send(new ASdu(TypeId.C_IC_NA_1,true, 1, CauseOfTransmission.SPONTANEOUS, false, false,
                    0, 1, bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
