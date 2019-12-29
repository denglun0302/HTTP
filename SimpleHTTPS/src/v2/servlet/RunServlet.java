package v2.servlet;

import v2.Request;
import v2.Response;

import java.io.IOException;

public class RunServlet extends HttpServlet {
    @Override
    public void doGet(Request req, Response resp) throws IOException {
        resp.println("<script src='joke.js'></script>");
    }
}
