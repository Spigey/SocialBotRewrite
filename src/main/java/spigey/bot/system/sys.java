package spigey.bot.system;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import com.nulabinc.zxcvbn.*;
import spigey.bot.DiscordBot;
// compile 'com.nulab-inc:zxcvbn:1.9.0'

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

    public static void ln(Object message) {System.out.println(message);}
    public static void log(Object message){
        System.out.print(message);
    }
    public static void debug(Object content) {
        ln("\u001B[42;30m[DEBUG]: " + content + " \u001B[49m\u001B[0m");
        try {
            DiscordBot.console.sendMessage("```[DEBUG]: " + content + "```").queue();
        }catch (Exception L){/**/}
    }

    public static void error(Object content) {
        ln("\u001B[41;30m[ERROR]: " + content + "\u001B[0m");
        try{
            DiscordBot.console.sendMessage("```[ERROR]: " + content + "```").queue();
        }catch (Exception L){/**/}
    }

    public static void warn(Object content) {
        ln("\u001B[43;30m[WARN]: " + content + "\u001B[0m");
        try{
            DiscordBot.console.sendMessage("```[WARN]: " + content + "```").queue();
        }catch (Exception L){/**/}
    }

    public static String encrypt(String text, String encryptionKey) throws Exception {
        SecretKey secretKey = generateKey(encryptionKey);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String trim(String str, int length) {
        return str.length() > length ? str.substring(0, length - 4) + "..." : str;
    }

    public static String mirt(String str, int length) {
        return str.length() > length ? "..." + str.substring(str.length() - length, str.length() - 1): str;
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

    public static String strOrDefault(@Nullable String str, String def){
        if(str == null){
            return def;
        }
        return str;
    }

    public static String generateToken(String username, String password, int length) throws NoSuchAlgorithmException {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._%$#@;:,&=+!";
        StringBuilder sb = new StringBuilder();
        String seed = String.format(env.TOKEN_SEED, username, password, generateKey(env.ENCRYPTION_KEY));
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
        Zxcvbn ps = new Zxcvbn();
        Strength strength = ps.measure(password);
        double guesses = strength.getGuesses();
        double logGuesses = Math.log10(guesses);
        return Double.parseDouble(String.format("%.2f",Math.min((logGuesses / 14) * 100, 100)));
    }
    public static String sendApiRequest(String url, String method, Map<String, String> headers, String body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));

        // Add default headers for common API interactions
        builder.header("Accept", "application/json"); // Assume JSON response by default
        builder.header("User-Agent", "MyJavaApp/1.0"); // Identify your application

        // Add any additional custom headers
        if (headers != null) {
            headers.forEach(builder::header);
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body(); // Directly return the response body
    }

    public static void empty(Object nothing){}

    public static Object choice(Object[] choices) {
        Random random = new Random();
        return choices[random.nextInt(choices.length)];
    }

    public static int occur(String text, String chr){
        return text.replaceAll("[^" + chr + "]", "").length() / chr.length();
    }

    public static String getExcept(Object[] arr, int i, String seperator){
        StringBuilder sb = new StringBuilder();
        for(int j = 0; j < arr.length; j++){
            if(j != i){
                sb.append(arr[j]).append(seperator);
            }
        }
        return sb.toString();
    }

    public static int codeLines(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        int lineCount = 0;

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                lineCount += codeLines(file.getAbsolutePath()); // Recurse into subdirectories
            } else if (file.getName().endsWith(".java")) {
                lineCount += Files.readAllLines(Path.of(file.getAbsolutePath())).size();
            }
        }
        return lineCount;
    }

    public static int codeChars(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        int charCount = 0;

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                charCount += codeChars(file.getAbsolutePath()); // Recurse into subdirectories
            } else if (file.getName().endsWith(".java")) {
                List<String> lines = Files.readAllLines(Path.of(file.getAbsolutePath()));
                for(String line : lines){
                    charCount += line.length();
                }
            }
        }
        return charCount;
    }


    public static double fileSize(String path){
        return (double) new File(path).length() / (1024 * 1024);
    }

    public static String replaceXth(String input, String target, String replacement, int interval) {
        if (interval < 1 || target.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        int count = 0;
        int i = 0;
        while (i < input.length()) {
            int foundIndex = input.indexOf(target, i);
            if (foundIndex == -1) {result.append(input.substring(i));break;}
            count++;
            if (count % interval == 0) {result.append(input, i, foundIndex).append(replacement);} else {result.append(input, i, foundIndex + target.length());}
            i = foundIndex + target.length();
        }
        return result.toString();
    }
}
