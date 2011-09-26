package org.tangram.view;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;

public interface ModelAwareViewResolver extends Ordered {

    View resolveViewName(String viewName, Map<String, Object> model, Locale locale) throws IOException;

} // ModelAwareViewResolver
