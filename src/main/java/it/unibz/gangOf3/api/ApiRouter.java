package it.unibz.gangOf3.api;

import it.unibz.gangOf3.api.auth.AuthRouter;
import it.unibz.gangOf3.api.store.StoreRouter;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class ApiRouter {

    public static void registerRoutes(Tomcat tomcat, Context context) {
        String prefix = "/api";

        // auth
        AuthRouter.registerRoutes(tomcat, context, prefix);

        // store
        StoreRouter.registerRoutes(tomcat, context, prefix);
    }

}
