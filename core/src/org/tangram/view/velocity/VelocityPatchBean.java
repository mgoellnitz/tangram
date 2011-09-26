package org.tangram.view.velocity;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.tangram.content.BeanFactory;

public class VelocityPatchBean implements InitializingBean {

    @Autowired
    BeanFactory factory;


    @Override
    public void afterPropertiesSet() throws Exception {
        VelocityResourceLoader.factory = factory;
    } // afterPropertiesSet()

} // VelocityPatchBean()
