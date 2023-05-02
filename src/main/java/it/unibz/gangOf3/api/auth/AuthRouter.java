package it.unibz.gangOf3.api.auth;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class AuthRouter {

    public static void registerRoutes(Tomcat tomcat, Context context, String prefix) {
        prefix += "/auth";

        //Register
        tomcat.addServlet(context, "registerServlet", new Register());
        context.addServletMappingDecoded(prefix + "/register", "registerServlet");
    }

}
