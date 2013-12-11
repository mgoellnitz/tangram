/**
 *
 * Copyright 2013 Martin Goellnitz
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
package org.tangram.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;


/**
 * Generic superclass for all implementations using items from the tangram repository - namely Apache Velocity
 * html or xml templates - as templates.
 *
 * @param <T> Type for the view technology in use e.g. Strings for filenames
 */
public abstract class AbstractRepositoryTemplateResolver<T extends Object> extends AbstractTemplateResolver<T> implements BeanListener {

    private final static Log log = LogFactory.getLog(AbstractRepositoryTemplateResolver.class);

    @Inject
    private CodeResourceCache codeResourceCache;

    private final Collection<String> supportedContenTypes;


    public AbstractRepositoryTemplateResolver() {
        super(false, ".");
        supportedContenTypes = new HashSet<String>();
        supportedContenTypes.add("text/html");
        supportedContenTypes.add("text/xml");
    } // AbstractRepositoryTemplateResolver()


    @Override
    protected T checkResourceExists(T view) {
        return view;
    }


    /**
     * Generic implementation of the plain template lookup in the tangram repository represented by the sub part
     * "code resource cache" in this case.
     *
     * @param path the path translates to the annotation in the code resource cache
     * @param locale the locale is ignored
     * @return CodeResource with one of the supported mime types and the given path as annotation
     * @throws Exception
     */
    protected CodeResource resolveTemplate(String path, Locale locale) throws Exception {
        CodeResource template = null;
        for (String type : supportedContenTypes) {
            if (template==null) {
                template = codeResourceCache.get(type, path);
            } // if
        } // for
        return template;
    } // resolveTemplate()


    @Override
    public void reset() {
        getCache().clear();
    } // reset()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        codeResourceCache.addListener(this);
    } // afterPropertiesSet()


    @Override
    public int compareTo(T o) {
        return -1;
    } // compareTo()

} // AbstractRepositoryTemplateResolver
