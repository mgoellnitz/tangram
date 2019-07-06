/**
 *
 * Copyright 2011-2016 Martin Goellnitz
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.jsp.LinkTag;

/**
 * Apache Velocity directive to generate links using the JSP tag implementation for the same purpose.
 */
public class LinkDirective implements TemplateDirectiveModel {

    private static final Logger LOG = LoggerFactory.getLogger(LinkDirective.class);


    @Override
    public void execute(Environment e, Map map, TemplateModel[] tms, TemplateDirectiveBody tdb) throws TemplateException, IOException {
        HttpServletRequest request = null; // TODO
        HttpServletResponse response = null; // TODO
        LinkFactoryAggregator builder = (LinkFactoryAggregator)e.getGlobalVariable(Constants.ATTRIBUTE_LINK_FACTORY_AGGREGATOR);

        /* getting direct parameters */
        Object bean = map.get("bean");
        String view = null;
        String action = null;
        boolean href = false;
        boolean target = false;
        boolean handlers = false;

        if (map.containsKey("view")) {
            view = ""+map.get("view");
        }
        if (map.containsKey("action")) {
            action = ""+map.get("action");
        }
        if (map.containsKey("href")) {
            action = ""+map.get("href");
        }
        if (map.containsKey("target")) {
            target = (Boolean)map.get("target");
        }
        if (map.containsKey("handlers")) {
            handlers = (Boolean)map.get("handlers");
        }

        try {
            LinkTag.render(builder, request, response, e.getOut(), bean, action, view, href, target, handlers);
        } catch (RuntimeException rte) {
            LOG.error("render()", rte);
        } // try/catch
    } // execute()

} // LinkDirective
