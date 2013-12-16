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
package org.tangram.components.dinistiq;

import dinistiq.web.RegisterableServlet;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Named;
import org.tangram.servlet.DefaultServlet;


@Named
public class TangramDefaultServlet extends DefaultServlet implements RegisterableServlet {

    @Override
    public int getOrder() {
        return 10;
    } // getOrder()


    @Override
    public Set<String> getUriRegex() {
        Set<String> result = new HashSet<String>();
        result.add("/id_([A-Z][a-zA-Z]+:[0-9]+)");
        result.add("/id_([A-Z][a-zA-Z]+:[0-9]+)/view_(.*)");
        return result;
    } // getUriRegex()


    @Override
    public int compareTo(RegisterableServlet o) {
        return getOrder()-o.getOrder();
    } // compareTo()

} // TangramDefaultServlet
