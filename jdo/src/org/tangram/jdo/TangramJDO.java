package org.tangram.jdo;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class TangramJDO {

    private static Properties PROPERTIES = new Properties();

    /**
    * TODO: May be it is a godd idea to move this to EditingController
    *
     * writable properties which should not be altered by the upper layers or persisted
     */
    public static Set<String> SYSTEM_PROPERTIES;

    static {
        // SYSTEM_PROPERTIES:
        SYSTEM_PROPERTIES = new HashSet<String>();
        SYSTEM_PROPERTIES.add("manager");
        SYSTEM_PROPERTIES.add("beanFactory");
        // PROPERTIES:
        try {
            PROPERTIES.load(TangramJDO.class.getClassLoader().getResourceAsStream("tangram/jdo-build.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } // try/catch
    } // static

    private static final String PROPERTY_VERSION_BUILD = "version.build";

    private static final String VERSION_MAJOR = "0";

    private static final String VERSION_MINOR = "5";
    
    public static String getVersion() {
        return VERSION_MAJOR+"."+VERSION_MINOR+"."+PROPERTIES.getProperty(PROPERTY_VERSION_BUILD);
    }

} // Constants
