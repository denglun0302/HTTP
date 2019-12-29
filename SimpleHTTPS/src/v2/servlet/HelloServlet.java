package v2.servlet;

import v2.Request;
import v2.Response;

public class HelloServlet extends HttpServlet {

    @Override
    public void doGet(Request req, Response resp){
        resp.setHeader("Content-Type", "text/plain; charset=utf-8") ;
        resp.setHeader("X-room","201");
        resp.setHeader("Who is a cool girl","XJR");
        resp.print("你好世界");
    }
}
