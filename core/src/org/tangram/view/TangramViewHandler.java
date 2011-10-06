package org.tangram.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;

@Component
public class TangramViewHandler implements ViewHandler, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /** List of ViewResolvers used by this servlet */
    private List<ModelAwareViewResolver> modelAwareViewResolvers;

    private boolean detectAllModelAwareViewResolvers = true;


    @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public boolean isDetectAllModelAwareViewResolvers() {
        return detectAllModelAwareViewResolvers;
    }


    public void setDetectAllModelAwareViewResolvers(boolean detectAllModelAwareViewResolvers) {
        this.detectAllModelAwareViewResolvers = detectAllModelAwareViewResolvers;
    }


    /**
     * Initialize the ViewResolvers used by this class.
     * <p>
     * If no ViewResolver beans are defined in the BeanFactory for this namespace, we default to
     * InternalResourceViewResolver.
     */
    private void initViewResolvers(ApplicationContext context) {
        this.modelAwareViewResolvers = null;

        if (this.detectAllModelAwareViewResolvers) {
            // Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, ModelAwareViewResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
                    ModelAwareViewResolver.class, true, false);
            if ( !matchingBeans.isEmpty()) {
                this.modelAwareViewResolvers = new ArrayList<ModelAwareViewResolver>(matchingBeans.values());
                // We keep ViewResolvers in sorted order.
                OrderComparator.sort(this.modelAwareViewResolvers);
            } // if
        } else {
            try {
                ModelAwareViewResolver vr = context.getBean(DispatcherServlet.VIEW_RESOLVER_BEAN_NAME,
                        ModelAwareViewResolver.class);
                this.modelAwareViewResolvers = Collections.singletonList(vr);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default ViewResolver later.
            } // try/catch
        } // if
    } // initViewResolvers()


    @Override
	public void afterPropertiesSet() throws Exception {
        initViewResolvers(applicationContext);
    } // afterPropertiesSet()


    @Override
	public View resolveView(String viewName, Map<String, Object> model, Locale locale, ServletRequest request)
            throws IOException {
        View result = null;

        for (ModelAwareViewResolver viewResolver : this.modelAwareViewResolvers) {
            View view = viewResolver.resolveViewName(viewName, model, locale);
            if (view!=null) {
                result = view;
                break;
            } // if
        } // for

        return result;
    } // resolveViewName()

} // TangramViewHandler
