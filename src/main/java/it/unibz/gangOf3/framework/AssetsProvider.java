package it.unibz.gangOf3.framework;

import it.unibz.gangOf3.util.QueryParser;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.FileNameMap;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AssetsProvider extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        OutputStream out = resp.getOutputStream();

        String queryStr = req.getQueryString();
        if (queryStr == null) {
            out.write("{\"status\": \"error\", \"message\": \"No query provided\"}".getBytes(StandardCharsets.UTF_8));
            return;
        }

        Map<String, String> query = QueryParser.parseQuery(queryStr);
        String pathToAsset = query.get("name");

        InputStream assetFileIS = getClass().getResourceAsStream("/frontend/assets/" + pathToAsset);
        if (assetFileIS == null) {
            out.write("{\"status\": \"error\", \"message\": \"Asset not found\"}".getBytes(StandardCharsets.UTF_8));
            return;
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = assetFileIS.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();

        resp.setContentLength(buffer.size());
        FileNameMap fileNameMap = java.net.URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(pathToAsset);
        resp.setContentType(mimeType);

        buffer.writeTo(resp.getOutputStream());
        resp.getOutputStream().flush();
        resp.getOutputStream().close();
    }

}
