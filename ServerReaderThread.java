import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;

public class ServerReaderThread extends Thread {
    private Socket clientSocket;

    ServerReaderThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            int type = dis.readInt();
            switch (type) {
                case 1: // Login
                    String loginMsg = dis.readUTF();
                    String[] loginParts = loginMsg.split("\n");
                    boolean isAuthenticated = false;
                    if (loginParts.length == 2) {
                        System.out.println("Login attempt for user: " + loginParts[0]);
                        isAuthenticated = checkUser(loginParts[0], loginParts[1]);
                        System.out.println("Authentication result: " + isAuthenticated);
                    }
                    DataOutputStream dosLogin = new DataOutputStream(clientSocket.getOutputStream());
                    dosLogin.writeBoolean(isAuthenticated);
                    dosLogin.flush();
                    break;
                case 2: // Register
                    String regMsg = dis.readUTF();
                    String[] regParts = regMsg.split("\n");
                    boolean isRegistered = false;
                    if (regParts.length == 2) {
                        System.out.println("Register attempt for user: " + regParts[0]);
                        isRegistered = registerUser(regParts[0], regParts[1]);
                        System.out.println("Registration result: " + isRegistered);
                    }
                    DataOutputStream dosReg = new DataOutputStream(clientSocket.getOutputStream());
                    dosReg.writeBoolean(isRegistered);
                    dosReg.flush();
                    break;
                case 3: // Get Data
                    try { dis.readUTF(); } catch (Exception e) {} 
                    
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                    Path filePath = Paths.get(Constant.DATA_JSON_FILE);
                    System.out.println("Reading data from: " + filePath.toAbsolutePath());
                    if (Files.exists(filePath)) {
                        String content = new String(Files.readAllBytes(filePath), "UTF-8");
                        out.write(content);
                    } else {
                        System.out.println("Data file not found!");
                        out.write("[]");
                    }
                    out.newLine();
                    out.flush();
                    break;
                default:
                    break;
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private boolean checkUser(String username, String password) {
        try {
            Path filePath = Paths.get(Constant.USERS_JSON_FILE);
            if (!Files.exists(filePath)) {
                System.out.println("Users file not found at: " + filePath);
                return false;
            }

            String content = new String(Files.readAllBytes(filePath), "UTF-8");
            
            String targetUser = "\"username\": \"" + username + "\"";
            int userIndex = content.indexOf(targetUser);
            if (userIndex != -1) {
                int hashIndex = content.indexOf("\"passwordHash\": \"", userIndex);
                if (hashIndex != -1) {
                    int start = hashIndex + 17;
                    int end = content.indexOf("\"", start);
                    String storedHash = content.substring(start, end);
                    
                    String inputHash = hashPassword(password);
                    return storedHash.equalsIgnoreCase(inputHash);
                }
            } else {
                System.out.println("User not found in JSON.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean registerUser(String username, String password) {
        try {
            Path filePath = Paths.get(Constant.USERS_JSON_FILE);
            if (!Files.exists(filePath)) {
                System.out.println("Users file not found for registration.");
                return false;
            }

            String content = new String(Files.readAllBytes(filePath), "UTF-8");
            
            String targetUser = "\"username\": \"" + username + "\"";
            if (content.indexOf(targetUser) != -1) {
                System.out.println("User already exists.");
                return false;
            }

            String passwordHash = hashPassword(password);
            
            int lastBracket = content.lastIndexOf("]");
            if (lastBracket != -1) {
                String newUserJson = ",\n" +
                        "    {\n" +
                        "      \"username\": \"" + username + "\",\n" +
                        "      \"passwordHash\": \"" + passwordHash + "\"\n" +
                        "    }";
                
                // Handle empty array case
                if (content.trim().replace(" ", "").contains("\"users\":[]")) {
                     newUserJson = "\n" +
                            "    {\n" +
                            "      \"username\": \"" + username + "\",\n" +
                            "      \"passwordHash\": \"" + passwordHash + "\"\n" +
                            "    }\n";
                }
                
                String newContent = content.substring(0, lastBracket) + newUserJson + content.substring(lastBracket);
                Files.write(filePath, newContent.getBytes("UTF-8"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
