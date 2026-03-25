package imd.ufrn.common.protocol;

import java.util.HashMap;
import java.util.Map;

public class HttpParser {

    private String method;
    private String route;
    private final Map<String, String> headers;
    private String body;

    public HttpParser(String rawHttp) {
        this.headers = new HashMap<>();
        this.body = "";
        parse(rawHttp);
    }

    private void parse(String rawHttp) {
        if (rawHttp == null || rawHttp.trim().isEmpty()) {
            return;
        }

        String[] headerAndBody = rawHttp.split("\r\n\r\n", 2);
        String headSection = headerAndBody[0];
        
        if (headerAndBody.length > 1) {
            this.body = headerAndBody[1].trim();
        }

        String[] lines = headSection.split("\r\n");
        if (lines.length == 0) return;

        String firstLine = lines[0];
        String[] firstLineParts = firstLine.split(" ");
        if (firstLineParts.length >= 2) {
            this.method = firstLineParts[0];
            this.route = firstLineParts[1];
        }

        for (int i = 1; i < lines.length; i++) {
            String[] headerParts = lines[i].split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }
    }

    public String getMethod() { return method; }
    public String getRoute() { return route; }
    public String getBody() { return body; }
    
    public static String buildHttpResponse(String bodyContent) {
        return """
               HTTP/1.1 200 OK\r
               Content-Length: """ + bodyContent.length() + "\r\n" +
               "Content-Type: text/plain\r\n" +
               "\r\n" + 
               bodyContent;
    }
}