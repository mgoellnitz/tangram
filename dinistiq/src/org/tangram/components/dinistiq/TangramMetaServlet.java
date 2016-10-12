/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
package org.tangram.components.dinistiq;

import dinistiq.web.RegisterableServlet;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.servlet.MetaServlet;


@Named("metaServlet")
@Singleton
public class TangramMetaServlet extends MetaServlet implements RegisterableServlet {

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;


    @Override
    public int getOrder() {
        return 1000;
    } // getOrder()


    @Override
    public Set<String> getUrlPatterns() {
        Set<String> result = new HashSet<>();
        result.add(linkFactoryAggregator.getDispatcherPath()+"/");
        result.add(linkFactoryAggregator.getDispatcherPath()+"/*");
        return result;
    } // getUrlPatterns()


    @Override
    public int compareTo(RegisterableServlet o) {
        return getOrder()-o.getOrder();
    } // compareTo()

} // TangramMetaServlet
