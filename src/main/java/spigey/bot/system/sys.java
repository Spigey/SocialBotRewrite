package spigey.bot.system;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
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
        StringBuilder err = new StringBuilder(L + "\n   ");
        for(int i = 0; i < L.getStackTrace().length - 1; i++){
            err.append(L.getStackTrace()[i]).append("\n   ");
        }
        err.append(L.getStackTrace()[L.getStackTrace().length - 1]);
        error(err);
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

    public static String encrypt(String text, String encryptionKey) throws Exception {
        SecretKey secretKey = generateKey(encryptionKey);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText, String encryptionKey) throws Exception {
        try {
            SecretKey secretKey = generateKey(encryptionKey);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch(Exception L){
            error(L.getMessage());
            return encryptedText;
        }
    }

    private static SecretKey generateKey(String encryptionKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] hashedBytes = digest.digest(keyBytes);
        return new SecretKeySpec(hashedBytes, "AES");
    }

    public static String trim(String str, int length) {
        return str.length() > length ? str.substring(0, length) + "..." : str;
    }

    public static String strOrDefault(@Nullable String str, String def){
        if(str == null){
            return def;
        }
        return str;
    }

    public static String generateToken(String username, String password, int length) throws NoSuchAlgorithmException {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._%$#@;:,&=+!";
        StringBuilder sb = new StringBuilder();
        String seed = env.getTokenSeed(username, password, generateKey(env.ENCRYPTION_KEY));
        Random random = new Random(seed.hashCode());
        for(int i = 0; i < length; i++){
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String passToStr(String password, String mask){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < password.length(); i++) {
            sb.append(mask);
        }
        return sb.toString();
    }
    public static double passStrength(String password) {
        int score = 0;
        score += Math.min(password.length() >= 12 ? 5 : password.length() >= 8 ? 3 : 0, 5);
        score += password.matches(".*[a-z].*") ? 1 : 0;
        score += password.matches(".*[A-Z].*") ? 1 : 0;
        score += password.matches(".*[0-9].*") ? 1 : 0;
        score += password.matches(".*[!@#$%^&*()\\-+=].*") ? 2 : 0;

        return Math.min(10, score) * 10;
    }

}
