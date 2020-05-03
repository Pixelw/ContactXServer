package com.pixelw;

import java.util.Scanner;

public class Main {

    private static boolean running;
    private static IMController imController;

    public static void main(String[] args) {
        imController = new IMController();
        Scanner scanner = new Scanner(System.in);
        running = true;
        while (running) {
            parseCommand(scanner.nextLine());
        }

        System.out.println("Exit");
    }

    private static void parseCommand(String command) {
        switch (command) {
            case "stop":
            case "bye":
            case "exit":
                imController.finish();
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
            String[] args = arg.split(" ",2);
            imController.sendTextMsg(args[0],args[1]);
            return true;
        }
        return false;
    }
}
