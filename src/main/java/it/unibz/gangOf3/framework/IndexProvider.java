package it.unibz.gangOf3.framework;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class IndexProvider extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();

        InputStream indexFileIS = getClass().getResourceAsStream("/frontend/index.html");
        if (indexFileIS == null) {
            writer.println("{\"status\": \"error\", \"message\": \"Index not found\"}");
            return;
        }

        String indexFileContent = new String(indexFileIS.readAllBytes());
        writer.println(indexFileContent);
    }

}
