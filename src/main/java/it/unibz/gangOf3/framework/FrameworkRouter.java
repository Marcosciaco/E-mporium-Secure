package it.unibz.gangOf3.framework;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class FrameworkRouter {

    public static void registerRoutes(Tomcat tomcat, Context context) {
        String prefix = "/frontend";

        //Register IndexProvider servlet
        tomcat.addServlet("", "IndexProvider", new IndexProvider());
        context.addServletMappingDecoded("/", "IndexProvider");
        //Register ComponentProvider servlet
        tomcat.addServlet("", "ComponentProvider", new ComponentProvider());
        context.addServletMappingDecoded(prefix + "/component", "ComponentProvider");
        //Register AssetsProvider servlet
        tomcat.addServlet("", "AssetsProvider", new AssetsProvider());
        context.addServletMappingDecoded(prefix + "/assets", "AssetsProvider");
    }

}
