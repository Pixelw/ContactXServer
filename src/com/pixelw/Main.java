package com.pixelw;

import com.pixelw.net.SocketCore;

import java.util.Scanner;

public class Main {

    private static boolean running;

    private static SocketCore socketCore;

    public static void main(String[] args) {
        socketCore = new SocketCore();
        socketCore.bindPort();
        Scanner scanner = new Scanner(System.in);
        running = true;
        while (running) {
            parseCommand(scanner.next());
        }
        System.out.println("Exit");
    }

    private static void parseCommand(String command) {
        switch (command) {
            case "stop":
//                socketCore.closeConnection();
                running = false;
                break;
            default:
                if (!parseArgsCommand(command)) {
                    System.out.println("Unknown command");
                }
                break;
        }
    }

    private static boolean parseArgsCommand(String command) {
        if (command.startsWith("say ")) {
            String arg = command.substring(4);
//            socketCore.sendViaSocket(arg);
            return true;
        }
        return false;
    }
}
