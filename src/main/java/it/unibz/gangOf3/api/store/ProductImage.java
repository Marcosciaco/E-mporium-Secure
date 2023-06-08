package it.unibz.gangOf3.api.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unibz.gangOf3.model.classes.Product;
import it.unibz.gangOf3.model.classes.User;
import it.unibz.gangOf3.model.exceptions.NotFoundException;
import it.unibz.gangOf3.model.repositories.ProductRepository;
import it.unibz.gangOf3.util.AuthUtil;
import it.unibz.gangOf3.util.ResponsePreprocessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import static it.unibz.gangOf3.util.security.CSRFHandler.handleCSRF;

public class ProductImage extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("pid") == null || !req.getParameter("pid").matches("[0-9]+")) {
            resp.setStatus(400);
            return;
        }

        try {
            Product product = ProductRepository.getProductById(Integer.parseInt(req.getParameter("pid")));
            InputStream productImage = product.getImg();
            resp.setContentType("image/jpeg");
            byte[] buffer = new byte[1024];
            int read;
            while ((read = productImage.read(buffer)) != -1) {
                resp.getOutputStream().write(buffer, 0, read);
            }
            resp.getOutputStream().flush();
            resp.getOutputStream().close();
        } catch (Exception ex) {
            resp.setStatus(500);
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        if (req.getParameter("pid") == null || !req.getParameter("pid").matches("[0-9]+")) {
            resp.setStatus(400);
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        try {

            Product product = ProductRepository.getProductById(Integer.parseInt(req.getParameter("pid")));
            if (!product.getOwner().equals(user)){
                resp.setStatus(401);
                return;
            }
            //product.setImg(req.getPart("image").getInputStream());
            System.out.println(new String(req.getInputStream().readAllBytes()));
            resp.setStatus(200);
            response.set("status", mapper.valueToTree("ok"));
        } catch (SQLException | NotFoundException e) {
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
