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
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static it.unibz.gangOf3.util.security.CSRFHandler.handleCSRF;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
        maxFileSize = 1024 * 1024 * 50, // 50 MB
        maxRequestSize = 1024 * 1024 * 100 // 100 MB
)
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResponsePreprocessor.preprocessResponse(resp);
        if (!handleCSRF(req, resp)) return;

        User user = AuthUtil.getAuthedUser(req, resp);
        if (user == null) return; // AuthUtil already sent the response (401)

        Part part = req.getPart("image");

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

            try {
                product.setImg(part.getInputStream());
            }catch (Exception e){
                e.printStackTrace();
                response.set("status", mapper.valueToTree("error"));
                response.set("message", mapper.valueToTree(e.getMessage()));
                resp.setStatus(500);
                return;
            }

            resp.setStatus(200);
            response.set("status", mapper.valueToTree("ok"));
        } catch (SQLException | NotFoundException e) {
            e.printStackTrace();
            response.set("status", mapper.valueToTree("error"));
            response.set("message", mapper.valueToTree(e.getMessage()));
        }

        resp.getWriter().write(mapper.writeValueAsString(response));
    }
}
