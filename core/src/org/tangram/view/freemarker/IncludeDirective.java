/*
 *
 * Copyright 2019 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.view.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tangram.Constants;
import org.tangram.view.ViewUtilities;


/**
 *
 */
public class IncludeDirective implements TemplateDirectiveModel {

    @Override
    public void execute(Environment e, Map map, TemplateModel[] tms, TemplateDirectiveBody tdb) throws TemplateException, IOException {
        HttpServletRequest request = null; // TODO
        HttpServletResponse response = null; // TODO

        Object bean = map.get("bean");
        String view = (String) map.get("view");

        ViewUtilities viewUtilities = (ViewUtilities)e.getGlobalVariable(Constants.ATTRIBUTE_VIEW_UTILITIES);

        // copy model from original context
        Map<String, Object> model = viewUtilities.getViewContextFactory().createModel(bean, request, response);
//        Object[] keys = context.getKeys();
//        for (Object key : keys) {
//            String k = key.toString();
//            if (!model.containsKey(k)) {
//                model.put(k, context.get(k));
//            } // if
//        } // for
//        model.remove("springMacroRequestContext");

        viewUtilities.render(e.getOut(), model, view);
    }

}
