package v2.servlet;

import v2.Request;
import v2.Response;

import java.io.IOException;

public class NotFoundServlet extends HttpServlet {
    @Override
    public void doGet(Request req, Response resp) throws IOException {
        resp.setStatus("404 Not Found");
        resp.println("<h1>页面不存在</h1>");
    }
}
