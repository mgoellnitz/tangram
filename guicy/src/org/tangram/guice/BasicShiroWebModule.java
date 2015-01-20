/*
 * 
 * Copyright 2014-2015 Martin Goellnitz
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
package org.tangram.guice;

import com.google.inject.Provides;
import javax.servlet.ServletContext;
import org.apache.shiro.config.Ini;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.realm.text.IniRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Web modules to glue a minimal tangram editor related shiro configuration into the guice setup.
 *
 * Everything else for shiro is configured via the usual ini file called in this case guice/shiro.ini, which
 * must be placed on the class path.
 */
public class BasicShiroWebModule extends ShiroWebModule {

    private static final Logger LOG = LoggerFactory.getLogger(BasicShiroWebModule.class);

    private final String dispatcherPath;


    public BasicShiroWebModule(ServletContext sc, String dispatcherPath) {
        super(sc);
        this.dispatcherPath = dispatcherPath;
        LOG.info("() dispatcherPath={}", dispatcherPath);
    } // BasicShiroWebModule()


    /**
     * Just add the AUTHC filter to the filter chain.
     * this method just encapsules the suppress warning to have this section small.
     * @param url url to add filter to the filter chain for.
     */
    @SuppressWarnings("unchecked")
    private void addAuthcFilter(String url) {
        addFilterChain(url, AUTHC);
    } // addAuthcFilter()


    @Override
    protected void configureShiroWeb() {
        try {
            bindRealm().toConstructor(IniRealm.class.getConstructor(Ini.class));
        } catch (NoSuchMethodException e) {
            addError(e);
        } // try/catch
        String[] urlPrefixes = {"/edit**", "/importer**", "/link**", "/list**", "/store**", "shiro/login.jsp"};
        for (String urlPrefix : urlPrefixes) {
            String url = urlPrefix.startsWith("/") ? dispatcherPath+urlPrefix : "/"+urlPrefix;
            addAuthcFilter(url);
        } // for
    } // configureShiroWeb()


    @Provides
    public Ini loadShiroIni() {
        return Ini.fromResourcePath("classpath:guice/shiro.ini");
    } // loadShiroIni()

} // BasicShiroWebModule
