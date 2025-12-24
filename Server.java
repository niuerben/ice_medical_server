import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    public static void main(String[] args) throws IOException {
        System.out.println("====服务器已启动，监听端口 " + Constant.SERVER_PORT + "...====");
        try{
            ServerSocket serverSocket = new ServerSocket(Constant.SERVER_PORT);
            ExecutorService pool = new ThreadPoolExecutor(3, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    
            while (true) {
                System.out.println("等待客户端连接...");
                Socket clientSocket = serverSocket.accept();
                // 使用线程池执行任务，实现并发处理
                pool.execute(new ServerReaderThread(clientSocket));
                System.out.println("一个客户端连接成功: " + clientSocket.getInetAddress());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        
    }
}
