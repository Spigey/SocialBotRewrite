package spigey.bot.system;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.HashMap;

public class api {
    private static final Logger logger = LoggerFactory.getLogger(api.class);
    private static final JSONParser parser = new JSONParser();

    /**
     * Sends a GET request to a URL and extracts a value from the JSON response based on the specified path.
     *
     * @param URL The URL to send the GET request to.
     * @param Path A dot-separated path specifying the location of the value within the JSON response (e.g., "result.response", or just "response").
     * @return The extracted value as a String, or null if an error occurs or the value is not found.
     */
    public static String get(String URL, String Path) {
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create(URL)).GET().build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.error("API request failed with status code: {}", response.statusCode());
            }
            JSONObject json = (JSONObject) parser.parse(response.body());
            if (Path == null || Path.isEmpty()) return json.toJSONString();
            String[] keys = Path.split("\\.");
            Object current = json;
            for (String key : keys) {
                if (current instanceof JSONObject && ((JSONObject) current).containsKey(key)) {
                    current = ((JSONObject) current).get(key);
                } else if (current instanceof JSONArray) {
                    for (Object obj : (JSONArray) current) {
                        if (obj instanceof JSONObject && ((JSONObject) obj).containsKey(key)) return ((JSONObject) obj).get(key).toString();
                    }
                } else {
                    logger.warn("Invalid path in JSON: {}", Path);
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error getting value from API response: {}", sys.getStackTrace(e));
            return null;
        }
    }

    /**
     * Sends a POST request to a URL with the specified data.
     *
     * @param URL The URL to send the POST request to.
     * @param Data A map of key-value pairs to be sent as the POST request body.
     * @return The response body as a String, or null if an error occurs.
     */
    public static String post(String URL, Map<String, String> Data) {
        return post(URL, Data, new HashMap<>());
    }

    /**
     * Sends a POST request to a URL with the specified data and headers.
     *
     * @param URL The URL to send the POST request to.
     * @param Data A map of key-value pairs to be sent as the POST request body.
     * @param Headers A map of key-value pairs to be added as headers to the POST request.
     * @return The response body as a String, or null if an error occurs.
     */
    public static String post(String URL, Map<String, String> Data, Map<String, String> Headers) {
        try {
            StringBuilder bodyBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : Data.entrySet()) {
                if (!bodyBuilder.isEmpty()) {
                    bodyBuilder.append("&");
                }
                bodyBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(URL)).POST(HttpRequest.BodyPublishers.ofString(bodyBuilder.toString()));
            for (Map.Entry<String, String> entry : Headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
            return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            logger.error("Error sending POST request: {}", sys.getStackTrace(e));
            return null;
        }
    }

    /**
     * Sends a PUT request to a URL with the specified data.
     *
     * @param URL The URL to send the PUT request to.
     * @param Data A map of key-value pairs to be sent as the PUT request body.
     * @return The response body as a String, or null if an error occurs.
     */
    public static String put(String URL, Map<String, String> Data) {
        return put(URL, Data, new HashMap<>());
    }

    /**
     * Sends a PUT request to a URL with the specified data and headers.
     *
     * @param URL The URL to send the PUT request to.
     * @param Data A map of key-value pairs to be sent as the PUT request body.
     * @param Headers A map of key-value pairs to be added as headers to the PUT request.
     * @return The response body as a String, or null if an error occurs.
     */
    public static String put(String URL, Map<String, String> Data, Map<String, String> Headers) {
        try {
            StringBuilder bodyBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : Data.entrySet()) {
                if (!bodyBuilder.isEmpty()) {
                    bodyBuilder.append("&");
                }
                bodyBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(URL)).PUT(HttpRequest.BodyPublishers.ofString(bodyBuilder.toString()));
            for (Map.Entry<String, String> entry : Headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
            return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            logger.error("Error sending PUT request: {}", sys.getStackTrace(e));
            return null;
        }
    }

    /**
     * Sends a DELETE request to a URL.
     *
     * @param URL The URL to send the DELETE request to.
     * @return The response body as a String, or null if an error occurs.
     */
    public static String delete(String URL) {
        return delete(URL, new HashMap<>());
    }

    /**
     * Sends a DELETE request to a URL with the specified headers.
     *
     * @param URL The URL to send the DELETE request to.
     * @param Headers A map of key-value pairs to be added as headers to the DELETE request.
     * @return The response body as a String, or null if an error occurs.
     */
    public static String delete(String URL, Map<String, String> Headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(URL)).DELETE();
            for (Map.Entry<String, String> entry : Headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
            return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            logger.error("Error sending DELETE request: {}", sys.getStackTrace(e));
            return null;
        }
    }

    /**
     * Sends a PATCH request to a URL with the specified data.
     *
     * @param URL The URL to send the PATCH request to.
     * @param Data A map of key-value pairs to be sent as the PATCH request body.
     * @return The response body as a String, or null if an error occurs.
     */
    public static String patch(String URL, Map<String, String> Data) {
        return patch(URL, Data, new HashMap<>());
    }

    /**
     * Sends a PATCH request to a URL with the specified data and headers.
     *
     * @param URL The URL to send the PATCH request to.
     * @param Data A map of key-value pairs to be sent as the PATCH request body.
     * @param Headers A map of key-value pairs to be added as headers to the PATCH request.
     * @return The response body as a String, or null if an error occurs.
     */
    public static String patch(String URL, Map<String, String> Data, Map<String, String> Headers) {
        try {
            StringBuilder bodyBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : Data.entrySet()) {
                if (!bodyBuilder.isEmpty()) {
                    bodyBuilder.append("&");
                }
                bodyBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(URL)).method("PATCH", HttpRequest.BodyPublishers.ofString(bodyBuilder.toString()));
            for (Map.Entry<String, String> entry : Headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
            return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            logger.error("Error sending PATCH request: {}", sys.getStackTrace(e));
            return null;
        }
    }
}
