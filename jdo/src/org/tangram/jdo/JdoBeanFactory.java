package org.tangram.jdo;

import java.util.Collection;

import javax.jdo.PersistenceManager;

import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public interface JdoBeanFactory extends BeanFactory {

    String postprocessPlainId(Object id);


    Collection<Class<? extends Content>> getClasses();


    /**
     * clears cache only for entries given type. Never dare to issue changes for abstract classes or interfaces!
     * 
     * only relevant for the attached listeners
     * 
     * @param cls
     * @throws Exception
     */
    void clearCacheFor(Class<? extends Content> cls);
    
    PersistenceManager getManager();

} // JdoBeanFactory
