package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Authenticate {
    private static final String USERS_JSON_FILE = "storage/users.json";

    public static boolean checkUser(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        
        // Load users map
        Map<String, String> userStore = loadUsersFromJSON();
        
        // Check if user exists
        if (!userStore.containsKey(username)) {
            return false;
        }
        
        String storedHash = userStore.get(username);
        String inputHash = hashPassword(password);
        
        return storedHash.equalsIgnoreCase(inputHash);
    }

    private static Map<String, String> loadUsersFromJSON() {
        Map<String, String> users = new LinkedHashMap<>();
        File file = new File(USERS_JSON_FILE);
        
        if (!file.exists()) {
            System.out.println("User storage file not found: " + file.getAbsolutePath());
            return users;
        }
        
        try {
            String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
            
            // Regex to match "username": "value", "passwordHash": "value"
            String regex = "\"username\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"passwordHash\"\\s*:\\s*\"([^\"]+)\"";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            
            while (matcher.find()) {
                String u = matcher.group(1);
                String p = matcher.group(2);
                users.put(u, p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }
}
