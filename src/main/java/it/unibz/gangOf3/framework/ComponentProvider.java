package it.unibz.gangOf3.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.util.QueryParser;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

public class ComponentProvider extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponsePreprocessor.preprocessResponse(resp);

        PrintWriter writer = resp.getWriter();

        String queryStr = req.getQueryString();
        if (queryStr == null) {
            writer.println("{\"status\": \"error\", \"message\": \"No query provided\"}");
            return;
        }
        Map<String, String> query = QueryParser.parseQuery(queryStr);
        String pathToComponent = query.get("path");

        InputStream componentFileIS = getClass().getResourceAsStream("/frontend/" + pathToComponent + ".html");
        if (componentFileIS == null) {
            writer.println("{\"status\": \"error\", \"message\": \"Component not found\"}");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("status", "ok");

        String componentFileContent = new String(componentFileIS.readAllBytes());

        //Extract script tag contents and remove them from DOM to be injected separately in the frontend
        Document componentFile = Jsoup.parse(componentFileContent);
        Elements scripts = componentFile.getElementsByTag("script");
        StringBuilder javascript = new StringBuilder();
        for (int i = 0; i < scripts.size(); i++) {
            String script = scripts.get(i).html();
            javascript.append(script).append(";\n");
            scripts.get(i).remove();
        }
        response.put("js", javascript.toString());
        response.put("html", componentFile.body().html());

        writer.println(mapper.writeValueAsString(response));
    }

}
