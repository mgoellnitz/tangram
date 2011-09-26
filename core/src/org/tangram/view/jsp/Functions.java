package org.tangram.view.jsp;

import java.util.Collection;

public class Functions {

    public static boolean contains(Collection<? extends Object> collection, Object object) {
        return collection == null ? false : collection.contains(object);
    } // contains()

} // Functions
