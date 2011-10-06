package org.tangram.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;

public class DefaultModelAndViewFactory implements ModelAndViewFactory {


    @Override
	public Map<String, Object> createModel(Object bean, ServletRequest request, ServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(Constants.THIS, bean);
        model.put("request", request);
        model.put("response", response);
        return model;
    } // createModelAndView()


    @Override
	public ModelAndView createModelAndView(Map<String, Object> model, String view) {
        return model==null ? null : (new ModelAndView(view==null ? Constants.DEFAULT_VIEW : view, model));
    } // createModelAndView()


    @Override
	public ModelAndView createModelAndView(Object bean, String view, ServletRequest request, ServletResponse response) {
        Map<String, Object> model = createModel(bean, request, response);
        return createModelAndView(model, view);
    } // createModelAndView()


    @Override
	public ModelAndView createModelAndView(Object bean, ServletRequest request, ServletResponse response) {
        return createModelAndView(bean, null, request, response);
    } // createModelAndView()

} // DefaultModelAndViewFactory
