package spigey.bot.system;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
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
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.stream.Collectors;

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
    public static String getInput(String message) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(message);
        return scanner.nextLine();
    }

    public static void exitWithError(String errorMessage) {
        System.err.println(errorMessage);
        System.exit(1);
    }

    public static void sleep(long milliseconds) {
        try {Thread.sleep(milliseconds);}
        catch (InterruptedException e) {
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

    private static SecretKeySpec generateKey(String encryptionKey) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(encryptionKey.toCharArray(), encryptionKey.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public static String encrypt(String plaintext, String encryptionKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(encryptionKey), new GCMParameterSpec(128, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(ciphertext); // Combine IV and ciphertext
    }


    public static String decrypt(String ciphertext, String encryptionKey) throws Exception {
        try {
            String[] parts = ciphertext.split(":"); // Split at the delimiter
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] ct = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, generateKey(encryptionKey), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(ct));
        } catch (Exception e) {
            error(e.getMessage());
            return ciphertext;
        }
    }

    public static String trim(String s, int l) {
        return s.length() > l ? s.substring(0, l - 4) + "..." : s;
    }

    public static String mirt(String s, int l) {
        return s.length() > l ? "..." + s.substring(s.length() - l, s.length() - 1) : s;
    }

    public static String strOrDefault(@Nullable String str, String def){
        return str == null ? def : str;
    }

    public static String generateToken(String username, String password) throws NoSuchAlgorithmException {
        /* String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._%$#@;:,&=+!";
        StringBuilder sb = new StringBuilder();
        String seed = String.format(env.TOKEN_SEED, username, password, generateKey(env.ENCRYPTION_KEY));
        Random random = new Random(seed.hashCode());
        for(int i = 0; i < length; i++){
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString(); */
        return genDiscordToken(strToInt(username + password)); // I was too lazy
    }

    public static String passToStr(String password, String mask){
        return String.valueOf(mask).repeat(password.length());
    }
    public static double passStrength(String password) {
        return Double.parseDouble(String.format("%.2f",Math.min((Math.log10(new Zxcvbn().measure(password).getGuesses()) / 14) * 100, 100)));
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

    public static String getAtLeast(Object[] arr, int i, String seperator){
        StringBuilder sb = new StringBuilder();
        for(int j = 0; j < arr.length; j++){
            if(j > i - 1){
                sb.append(arr[j]).append(seperator);
            }
        }
        return sb.toString();
    }

    public static String trimMarkdown(String text, int index) {
        index = Math.min(index, text.split("\n").length);
        List<String> firstPosts = Arrays.asList(text.split("\n")).subList(0, index);
        return String.join("\n", firstPosts);
    }

    public static long strToInt(String input) {
        long number = 0;
        for (char c : input.toCharArray()) number = 31 * number + c;
        return number;
    }

    public static String genDiscordToken(long seed) {
        final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_";
        Random random = new Random(CHARS.hashCode());
        random.setSeed(seed);
        StringBuilder token = new StringBuilder(String.valueOf((char) (random.nextInt(26) + 'A')));
        for (int i = 0; i < 23; i++) token.append(CHARS.charAt(random.nextInt(CHARS.length())));
        token.append(".").append((char) (random.nextInt(26) + 'A'));
        for (int i = 0; i < 5; i++) token.append(CHARS.charAt(random.nextInt(CHARS.length())));
        token.append(".");
        for (int i = 0; i < 27; i++) token.append(CHARS.charAt(random.nextInt(CHARS.length())));
        return token.toString();
    }

    public static String hashToken(String token) {
        try {
            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(token.getBytes());
            Formatter formatter = new Formatter();
            for (byte b : hashBytes) formatter.format("%02x", b);
            return formatter.toString().substring(0, 6);
        } catch (Exception e) {
            sys.errInfo(e);
        }
        return null;
    }
}