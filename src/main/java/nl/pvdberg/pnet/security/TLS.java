package nl.pvdberg.pnet.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TLS
{
    public static String TLS_CONTEXT_PROTOCOL = "TLSv1.2";

    /**
     * Strong TLS protocols
     */
    public static List<String> TLS_PROTOCOLS = new ArrayList<String>();

    /**
     * Strong cipher suites (best to worst)
     */
    public static List<String> TLS_CIPHER_SUITES = new ArrayList<String>();

    static
    {
        TLS.TLS_PROTOCOLS.add("TLSv1.2");

        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256");
        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256");

        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");

        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");
        TLS.TLS_CIPHER_SUITES.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");

        TLS.TLS_CIPHER_SUITES.add("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384");
        TLS.TLS_CIPHER_SUITES.add("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256");
    }

    /**
     * Returns intersection of available and supported
     * @return Intersection of available and supported
     */
    static String[] getUsable(final List<String> available, final String[] supported)
    {
        final List<String> filtered = new ArrayList<String>(available.size());
        final List<String> supportedList = Arrays.asList(supported);

        for (final String s : available)
        {
            if (supportedList.contains(s)) filtered.add(s);
        }

        final String[] filteredArray = new String[filtered.size()];
        filtered.toArray(filteredArray);
        return filteredArray;
    }

    /**
     * Enables SSL debug output
     */
    public static void setSSLDebug()
    {
        System.setProperty("javax.net.debug", "SSL");
    }
}
