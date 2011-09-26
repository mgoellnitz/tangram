package org.tangram.content;

import java.util.List;

public interface BeanFactory {

    <T extends Content> T getBean(Class<T> cls, String id);


    Content getBean(String id);


    /**
     * 
     * You MUST call persist() when using this method
     * 
     * @param <T>
     * @param cls
     * @param id
     * @return
     */
    <T extends Content> T getBeanForUpdate(Class<T> cls, String id);


    /**
     * 
     * You MUST call persist() when using this method
     * 
     * @param id
     * @return
     */
    Content getBeanForUpdate(String id);


    /**
     * 
     * @param cls
     *            beans class to query for
     * @param optionalQuery
     *            query string specific to the underlying storage system
     * @param orderProperty
     *            name of a attribute of the bean to be used for ascending ordering
     * @return
     */
    <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery, String orderProperty);


    <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery);


    <T extends Content> List<T> listBeans(Class<T> cls);


    <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String optionalQuery, String orderProperty);


    <T extends Content> List<T> listBeansOfExactClass(Class<T> cls);


    <T extends Content> T createBean(Class<T> cls) throws Exception;


    /**
     * trigger this listener if changes to beans of given type occur
     * 
     * @param map
     */
    void addListener(Class<? extends Content> cls, BeanListener listener);

} // BeanFactory
