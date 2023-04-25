package fr.ramatellier.greed.server.util.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * this class takes a http link and adapt it to a httpClient request
 */
public final class CommandRequestAdapter {
    private CommandRequestAdapter(){}

    /**
     * adapt a http request to a httpClient request
     * example:
     * 'http://www-igm.univ-mlv.fr/~carayol/Factorizer.jar' becomes
     * path: '/~carayol/Factorizer.jar' and request: 'GET /~carayol/Factorizer.jar HTTP/1.1\r\nHost: www-igm.univ-mlv.fr\r\n\r\n'
     * @param request
     * @return
     */
    public static PathRequest adapt(URL request){
        var path = request.getPath();
        var host = request.getHost();
        var requestString = "GET " + path + " HTTP/1.1\r\nHost: " + host + "\r\n\r\n";
        System.out.println("Path: " + path + " request: " + requestString);
        return new PathRequest("http://"+host, requestString, Path.of(path).getFileName().toString());
    }

    public static void main(String[] args) throws MalformedURLException {
        var request = adapt(new URL("http://www-igm.univ-mlv.fr/~carayol/Factorizer.jar"));
        System.out.println(request);
    }
}
