import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SimpleHTTPServer {
    private static class Task implements Runnable {
        private final Socket socket;

        Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                // 解析请求
                Request request = Request.parse(is);
                System.out.println(request);
                // 处理业务
            if(request.path.equalsIgnoreCase("/")) {
                String body = "<h1>一切正常</h1>";
                // 拼接响应
                byte[] bodyBuffer = body.getBytes("UTF-8");
                StringBuffer response = new StringBuffer();
                response.append("HTTP/1.0 200 OK\r\n");
                response.append("Content - Type: text/html; charset=UTF-8\r\n");
                response.append("Content - Length: ");
                response.append("bodyBuffer.length");
                response.append("\r\n");
               // response .append("HTTP/1.0 307 Temporary Redirect\r\n");
                //response.append("Location: /run\r\n");
                //跳转

                response.append("\r\n");
                os.write(response.toString().getBytes("UTF-8"));
                os.write(bodyBuffer);
                os.flush();
                // 发送响应
            }else{
                StringBuffer response = new StringBuffer();
                response.append("HTTP/1.0 404 NOT Found\r\n");
                response.append("\r\n");
                os.write(response.toString().getBytes("UTF-8"));
                os.flush();
            }


            socket.close();
            } catch (Exception e) { }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(80);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        while (true) {
            Socket socket = serverSocket.accept();
            pool.execute(new Task(socket));
        }
    }
}

