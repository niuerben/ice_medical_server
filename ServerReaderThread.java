import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerReaderThread extends Thread {
    private Socket clientSocket;

    ServerReaderThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        try {
            // 创建输出流
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            // 读取 JSON 文件内容
            Path filePath = Paths.get("storage/data.json");
            String content = new String(Files.readAllBytes(filePath), "UTF-8");
            // 打印读取的内容（可选）
            System.out.println("读取到的数据: " + content);            
            // JSON内容写入到输出流
            out.write(content);
            out.newLine();
            out.flush();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
