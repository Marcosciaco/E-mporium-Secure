package it.unibz.gangOf3.api.auth;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class AuthRouter {

    public static void registerRoutes(Tomcat tomcat, Context context, String prefix) {
        prefix += "/auth";

        //Register
        tomcat.addServlet(context, "registerServlet", new Register());
        context.addServletMappingDecoded(prefix + "/register", "registerServlet");

        //Login
        tomcat.addServlet(context, "loginServlet", new Login());
        context.addServletMappingDecoded(prefix + "/login", "loginServlet");

        tomcat.addServlet(context, "redeemServlet", new Redeem());
        context.addServletMappingDecoded(prefix + "/redeem", "redeemServlet");

        tomcat.addServlet(context, "forgotServlet", new Forgot());
        context.addServletMappingDecoded(prefix + "/forgot", "forgotServlet");
    }

}
