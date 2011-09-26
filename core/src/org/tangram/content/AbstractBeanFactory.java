package org.tangram.content;

import java.util.List;

public abstract class AbstractBeanFactory implements BeanFactory {

    @Override
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls) {
        return listBeansOfExactClass(cls, null, null);
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery) {
        return listBeans(cls, optionalQuery, null);
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls) {
        return listBeans(cls, null, null);
    } // listBeans()

} // AbstractBeanFactory
