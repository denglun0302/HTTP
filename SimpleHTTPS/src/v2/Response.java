package v2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Response {
    String status = "200 OK";
    Map<String, String> headers = new HashMap<>();
    StringBuilder bodyBuilder = new StringBuilder();

    Response() {
        headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void print(String s) {
        bodyBuilder.append(s);
    }

    public void println(String s) {
        bodyBuilder.append(s);
        bodyBuilder.append("\r\n");
    }

    void writeAndFlush(OutputStream os) throws IOException {
        // 1. 组装响应
        String response = buildResponse();
        System.out.println("准备发送的响应: \r\n" + response);
        os.write(response.getBytes("UTF-8"));
        os.flush();
    }

    private String buildResponse() throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        // 响应行
        responseBuilder.append("HTTP/1.0 ");
        responseBuilder.append(status);
        responseBuilder.append("\r\n");
        // 响应头
        int contentLength = bodyBuilder.toString().getBytes("UTF-8").length;
        setHeader("Content-Length", String.valueOf(contentLength));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            responseBuilder.append(entry.getKey());
            responseBuilder.append(": ");
            responseBuilder.append(entry.getValue());
            responseBuilder.append("\r\n");
        }
        // 空行
        responseBuilder.append("\r\n");
        // body
        responseBuilder.append(bodyBuilder);

        return responseBuilder.toString();
    }
}
