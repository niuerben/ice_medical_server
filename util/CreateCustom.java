package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCustom {
    private static final String USERS_JSON_FILE = "storage/users.json";

    public static boolean checkUserExists(String username) {
        Map<String, String> users = loadUsersFromJSON();
        return users.containsKey(username);
    }

    public static void addNewUser(String username, String password) {
        Map<String, String> users = loadUsersFromJSON();
        String passwordHash = hashPassword(password);
        users.put(username, passwordHash);
        saveUsersToJSON(users);
    }

    private static Map<String, String> loadUsersFromJSON() {
        Map<String, String> users = new LinkedHashMap<>();
        File file = new File(USERS_JSON_FILE);
        
        if (!file.exists()) {
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

    private static void saveUsersToJSON(Map<String, String> users) {
        try {
            File file = new File(USERS_JSON_FILE);
            // Ensure directory exists
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"users\": [\n");
            
            int count = 0;
            for (Map.Entry<String, String> entry : users.entrySet()) {
                if (count > 0) {
                    json.append(",\n");
                }
                json.append("    {\n");
                json.append("      \"username\": \"").append(entry.getKey()).append("\",\n");
                json.append("      \"passwordHash\": \"").append(entry.getValue()).append("\"\n");
                json.append("    }");
                count++;
            }
            
            json.append("\n  ]\n");
            json.append("}\n");
            
            Files.write(file.toPath(), json.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            System.err.println("Failed to save user data: " + e.getMessage());
            e.printStackTrace();
        }
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
