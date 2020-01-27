public class JokeJSServlet extends HttpServlet {
    @Override
    public void doGet(Request req, Response resp) {
        resp.setStatus("200 OK");
        resp.setHeader("Content-Type", "application/javascript; charset=UTF-8");
        resp.println("alert('很好很好');");
    }
    public void loginGet(Request req, Response resp)
    {
        String username=req.parameters.get("username");
        if(username==null){
            resp.setStatus("401 Unauthorized");
            resp.println("<h1>请登录</h1>");
            return;
        }
        resp.setHeader("Set-Cookie","username"+username);
        resp.print("<h1>欢迎"+username+"光临</h1>");
    }
}
