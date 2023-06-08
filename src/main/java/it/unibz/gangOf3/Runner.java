package it.unibz.gangOf3;

import it.unibz.gangOf3.api.ApiRouter;
import it.unibz.gangOf3.email.EmailSender;
import it.unibz.gangOf3.framework.ComponentProvider;
import it.unibz.gangOf3.framework.FrameworkRouter;
import it.unibz.gangOf3.util.DatabaseUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class Runner {

    public static String[] args;

    public static void main(String[] args) throws LifecycleException, IOException {
        Runner.args = args;

        //Initialize database

        DatabaseUtil.init();
        System.out.println("💾 Database initialized!");

        //Initialize email sender

        EmailSender.init();
        System.out.println("📧 EmailSender initialized!");

        //Setup Tomcat

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        tomcat.setPort(8443);
        Connector connector = getHttpsConnector();
        connector.setMaxPostSize(-1);
        ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setAllowedTrailerHeaders(FileUpload.MULTIPART_FORM_DATA);
        tomcat.setConnector(connector);

        // Configure the security constraint for HTTPS
        SecurityCollection securityCollection = new SecurityCollection();
        securityCollection.addPattern("/*");
        SecurityConstraint securityConstraint = new SecurityConstraint();
        securityConstraint.setUserConstraint("CONFIDENTIAL");
        securityConstraint.addCollection(securityCollection);

        // Register routes
        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);
        context.addConstraint(securityConstraint);
        context.setAllowCasualMultipartParsing(true);

        FrameworkRouter.registerRoutes(tomcat, context);
        ApiRouter.registerRoutes(tomcat, context);

        tomcat.start();
        System.out.println("📡 HTTPS Tomcat Embedded listening on port 8443!");
        tomcat.getServer().await();
    }

    private static Connector getHttpsConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(8443);
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setProperty("SSLEnabled", "true");

        // Configure SSL
        SSLHostConfig sslHostConfig = new SSLHostConfig();
        SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
        certificate.setCertificateFile("../certificate.crt");
        certificate.setCertificateKeyFile("../certificate.key");
        sslHostConfig.addCertificate(certificate);
        connector.addSslHostConfig(sslHostConfig);

        return connector;
    }

    public static boolean containsArg(String arg) {
        for (String s : args) {
            if (s.equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
