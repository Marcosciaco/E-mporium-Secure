package it.unibz.gangOf3.api.chat;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class ChatRouter {

    public static void registerRoutes(Tomcat tomcat, Context context, String prefix) {
        prefix += "/chat";

        //Message
        tomcat.addServlet(context, "messageServlet", new Message());
        context.addServletMappingDecoded(prefix + "/message", "messageServlet");

        //Feed
        tomcat.addServlet(context, "feedServlet", new Feed());
        context.addServletMappingDecoded(prefix + "/feed", "feedServlet");

        //Chat
        tomcat.addServlet(context, "keyServlet", new Key());
        context.addServletMappingDecoded(prefix + "/key", "keyServlet");
    }

}
