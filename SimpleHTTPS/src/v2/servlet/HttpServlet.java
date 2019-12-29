package v2.servlet;

import v2.Request;
import v2.Response;

import java.io.IOException;

public abstract class HttpServlet {
    public abstract void doGet(Request req, Response resp) throws IOException;
}
