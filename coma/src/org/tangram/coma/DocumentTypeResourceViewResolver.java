/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.coma;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;
import org.tangram.spring.view.ModelAwareInternalResourceViewResolver;

public class DocumentTypeResourceViewResolver extends ModelAwareInternalResourceViewResolver {

    private static final Log log = LogFactory.getLog(DocumentTypeResourceViewResolver.class);

    private String packageName;

    private Map<String, String> parents = new HashMap<String, String>();

    private Map<String, String> packages = new HashMap<String, String>();


    public String getPackageName() {
        return packageName;
    }


    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public Map<String, String> getParents() {
        return parents;
    }


    public void setParents(Map<String, String> parents) {
        this.parents = parents;
    }


    public Map<String, String> getPackages() {
        return packages;
    }


    public void setPackages(Map<String, String> packages) {
        this.packages = packages;
    }


    @Override
    protected View lookupView(String viewName, Locale locale, Object content, String key) throws IOException {
        View view = null;
        if (content instanceof ComaContent) {
            ComaContent cc = (ComaContent)content;
            if (log.isWarnEnabled()) {
                log.warn("lookupView() have coma content "+cc.getId()+" :"+cc.getDocumentType());
            } // if
            String documentType = cc.getDocumentType();
            while ((view==null)&&(documentType!=null)) {
                String packName = packages.get(documentType);
                if (packName==null) {
                    packName = this.packageName;
                } // if
                if (log.isWarnEnabled()) {
                    log.warn("lookupView() checking "+packName+"/"+documentType+"#"+viewName);
                } // if
                view = checkView(viewName, packName, documentType, key, locale);
                if (log.isWarnEnabled()) {
                    log.warn("lookupView() result "+view);
                } // if
                  // TODO: How about fake interfaces?
                  // if (view==null) {
                  // for (Class<? extends Object> c : cls.getInterfaces()) {
                  // view = checkView(viewName, c.getPackage().getName(), c.getSimpleName(), key, locale);
                  // if (view!=null) {
                  // break;
                  // } // if
                  // } // for
                  // } // if
                documentType = parents.get(documentType);
            } // while
        } // if
        return view;
    }// lookupView()

} // FolderTemplateRepository
