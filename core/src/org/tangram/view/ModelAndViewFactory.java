package org.tangram.view;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.servlet.ModelAndView;

public interface ModelAndViewFactory {

    Map<String, Object> createModel(Object bean, ServletRequest request, ServletResponse response);


    ModelAndView createModelAndView(Map<String, Object> model, String view);


    ModelAndView createModelAndView(Object bean, String view, ServletRequest request, ServletResponse response);


    ModelAndView createModelAndView(Object bean, ServletRequest request, ServletResponse response);

} // ModelAndViewFactory
