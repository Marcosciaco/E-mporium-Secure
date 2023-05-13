package it.unibz.gangOf3.api.store;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class StoreRouter {

    public static void registerRoutes(Tomcat tomcat, Context context, String prefix) {
        prefix += "/store";

        //Product
        tomcat.addServlet(context, "productServlet", new Product());
        context.addServletMappingDecoded(prefix + "/product", "productServlet");

        //Review
        tomcat.addServlet(context, "reviewServlet", new Review());
        context.addServletMappingDecoded(prefix + "/review", "reviewServlet");
    }

}
