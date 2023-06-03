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

        //Redeem
        tomcat.addServlet(context, "redeemServlet", new Redeem());
        context.addServletMappingDecoded(prefix + "/redeem", "redeemServlet");

        //Forgot
        tomcat.addServlet(context, "forgotServlet", new Forgot());
        context.addServletMappingDecoded(prefix + "/forgot", "forgotServlet");

        //Logout
        tomcat.addServlet(context, "logoutServlet", new Logout());
        context.addServletMappingDecoded(prefix + "/logout", "logoutServlet");

        //User
        tomcat.addServlet(context, "userServlet", new User());
        context.addServletMappingDecoded(prefix + "/user", "userServlet");
    }

}
