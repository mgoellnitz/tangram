package org.tangram.rdbms;

import java.util.Properties;

public class TangramRDBMS {

    private static Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(TangramRDBMS.class.getClassLoader().getResourceAsStream("tangram/rdbms-build.properties"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } // try/catch
    } // static

    private static final String PROPERTY_VERSION_BUILD = "version.build";

    private static final String VERSION_MAJOR = "0";

    private  static final String VERSION_MINOR = "5";
    
    public static String getVersion() {
        return VERSION_MAJOR+"."+VERSION_MINOR+"."+PROPERTIES.getProperty(PROPERTY_VERSION_BUILD);
    }

} // Constants
