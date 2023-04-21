package it.unibz.gangOf3;

import it.unibz.gangOf3.framework.ComponentProvider;
import it.unibz.gangOf3.framework.FrameworkRouter;
import it.unibz.gangOf3.util.DatabaseUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Runner {
    public static void main(String[] args) throws LifecycleException {

        //Initialize database

        DatabaseUtil.init();
        System.out.println("💿 Database initialized!");

        //Setup Tomcat

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        tomcat.setPort(8080);
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);

        FrameworkRouter.registerRoutes(tomcat, context);

        tomcat.start();
        System.out.println("📡 Tomcat Embedded listening on port 8080!");
        tomcat.getServer().await();
    }
}
