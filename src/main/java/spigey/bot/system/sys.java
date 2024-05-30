package spigey.bot.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class sys {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_MAGENTA = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BG_BLACK = "\u001B[40m";
    public static final String ANSI_BG_RED = "\u001B[41m";
    public static final String ANSI_BG_GREEN = "\u001B[42m";
    public static final String ANSI_BG_YELLOW = "\u001B[43m";
    public static final String ANSI_BG_BLUE = "\u001B[44m";
    public static final String ANSI_BG_MAGENTA = "\u001B[45m";
    public static final String ANSI_BG_CYAN = "\u001B[46m";
    public static final String ANSI_BG_WHITE = "\u001B[47m";

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    public static String exec(String cmd){
        try {
            Process proc = new ProcessBuilder("cmd", "/c", cmd).redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        } catch(Exception L){
            errInfo(L);
            return null;
        }
    }
    public static String getUserInput(String message) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(message);
        return scanner.nextLine();
    }

    public static void exitWithError(String errorMessage) {
        System.err.println(errorMessage);
        System.exit(1);
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            errInfo(e);
        }
    }
    public static void errInfo(Exception L){
        error(L.getMessage());
        error(L.getStackTrace());
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }

    // File functions

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.mkdirs();
    }

    // Logging functions

    public static void ln(Object message) {
        System.out.println(message);
    }
    public static void log(Object message){
        System.out.print(message);
    }
    public static void debug(Object content) {
        ln("\u001B[42;30m[DEBUG]: " + content + " \u001B[49m");
    }

    public static void error(Object content) {
        ln("\u001B[41;30m[ERROR]: " + content + "\u001B[0m");
    }

    public static void warn(Object content) {
        ln("\u001B[43;30m[WARN]: " + content + "\u001B[0m");
    }
}
