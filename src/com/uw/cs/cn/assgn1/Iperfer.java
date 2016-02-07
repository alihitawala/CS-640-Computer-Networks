//package com.uw.cs.cn.assgn1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aliHitawala on 1/20/16.
 */
public class Iperfer {
    private static Map<String, String> argMap = new HashMap<>();
    private static String ERROR_WRONG_ARG = "Error: missing or additional arguments";
    private static String ERROR_PORT_RANGE = "Error: port number must be in the range 1024 to 65535";

    public static void main(String[] args) {
        ArgumentHandler argumentHandler = new ArgumentHandler(args);
        argMap = argumentHandler.getOptionsMap();
        if (argumentHandler.isServer()) {
            try {
                new Server(Integer.parseInt(argMap.get("p"))).start();
            } catch (Exception e) {
                System.out.println("Server couldn't be started!");
            }
        } else {
            try {
                new Client(argMap.get("h"), Integer.parseInt(argMap.get("p")), Double.parseDouble(argMap.get("t"))).start();
            } catch (Exception e) {
                System.out.println("Client connection failed! \n" + e.getMessage());
            }
        }
    }

    static class Server {
        private final int port;
        private final byte[] incoming = new byte[1000];

        public Server(int port) {
            this.port = port;
        }

        public void start() throws IOException {
            ServerSocket serverSocket = new ServerSocket(this.port);
            Socket client = serverSocket.accept();
            DataInputStream inFromClient = new DataInputStream(client.getInputStream());
            long totalReceived = 0;
            long startTime = System.nanoTime();
            double totalTimeInReceiving = 0;
            try {
                while (true) {
                    inFromClient.readFully(incoming);
                    totalTimeInReceiving = (System.nanoTime() - startTime);
                    totalReceived++;
                }
            } catch (EOFException e) {
                //do nothing
            }
            totalTimeInReceiving = totalTimeInReceiving / Math.pow(10, 6);
            serverSocket.close();
            double rate = (totalReceived * 8.0) / (totalTimeInReceiving);
            System.out.println("received=" + totalReceived + " KB rate=" + rate + " Mbps");
        }
    }

    static class Client {
        private final String host;
        private final int port;
        private final double time;
        private final byte[] payload = new byte[1000];

        public Client(String host, int port, double time) {
            this.host = host;
            this.port = port;
            this.time = time;
        }

        public void start() throws IOException {
            Socket socket = new Socket(this.host, this.port);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            long totalWrites = 0;
            long endTime = System.nanoTime() +  (long) (this.time * Math.pow(10, 9));
            while (System.nanoTime() < endTime) {
                outputStream.write(payload);
                totalWrites++;
            }
            socket.close();
            double rate = (totalWrites * 8.0) / (1000 * this.time);
            System.out.println("sent=" + totalWrites + " KB rate=" + rate + " Mbps");
        }
    }

    static class ArgumentHandler {
        private Map<String, String> optionsMap = new HashMap<>();
        List<String> optionList = new ArrayList<>();

        public ArgumentHandler(String[] args) {
            String option = "";
            for (String arg : args) { // -s -p 9090 -t 9 -h host
                boolean isOption = arg.charAt(0) == '-';
                if (isOption) {
                    option = arg.substring(1);
                    optionList.add(option); // [s,p] , [c,p,t,h]
                } else {
                    optionsMap.put(option, arg);//[{p,9090}, {t, 9}, {h,host}]
                }
            }
            try {
                checkArguments(optionList, optionsMap);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }

        public Map<String, String> getOptionsMap() {
            return optionsMap;
        }

        public boolean isServer() {
            return optionList.contains("s");
        }

        private void checkArguments(List<String> optionList, Map<String, String> argMap) {
            boolean clientCondition = optionList.contains("c") && optionList.contains("h")
                    && optionList.contains("t") && optionList.contains("p") && optionList.size() == 4 && argMap.containsKey("h") && argMap.containsKey("p") && argMap.containsKey("t") && !argMap.containsKey("c");
            boolean serverCondition = optionList.contains("s") && optionList.size() == 2 && optionList.contains("p") && argMap.containsKey("p") && !argMap.containsKey("s");
            if (!clientCondition && !serverCondition) {
                throw new RuntimeException(ERROR_WRONG_ARG);
            }
            try {
                int port = Integer.parseInt(argMap.get("p"));
                if (port > 65535 || port < 1024) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                throw new RuntimeException(ERROR_PORT_RANGE);
            }
            if (!isServer()) {
                try {
                    double d = Double.parseDouble(argMap.get("t"));
                    if (d < 0)
                        throw new RuntimeException();
                } catch (Exception e) {
                    throw new RuntimeException("Invalid time entered!");
                }
            }
        }
    }
}