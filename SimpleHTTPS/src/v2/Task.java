package v2;

import v2.servlet.*;

import java.net.Socket;

public class Task implements Runnable {
    private Socket socket;

    public Task(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 读取并解析请求 -> 处理业务 -> 组装并发送响应

            // 1. 读取并解析请求
            Request request = Request.parse(socket.getInputStream());
            System.out.println(request);
            Response response = new Response();

            // 2. 处理业务
            HttpServlet servlet;
            if (request.path.equals("/joke.js")) {
                servlet = new JokeJSServlet();
            } else if (request.path.equals("/login")) {
                // /login?username=你的用户名
                // 发送响应: Header: Set-Cookie: username=你的用户名
                servlet = new LoginServlet();
            }else if(request.path.equals("/hello")){
                servlet=new HelloServlet();
            }
            else {
                servlet = new NotFoundServlet();
            }
            servlet.doGet(request, response);

            // 3. 组装并发送响应
            response.writeAndFlush(socket.getOutputStream());
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
