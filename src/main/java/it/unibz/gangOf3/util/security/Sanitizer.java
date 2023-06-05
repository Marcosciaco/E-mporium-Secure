package it.unibz.gangOf3.util.security;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class Sanitizer {

    private static PolicyFactory policyFactory = new HtmlPolicyBuilder().toFactory();

    public static String sanitize(String input) {
        return policyFactory.sanitize(input);
    }

}
