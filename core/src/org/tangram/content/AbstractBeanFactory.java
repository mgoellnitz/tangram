/**
 * 
 * Copyright 2011-2012 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.content;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractBeanFactory implements BeanFactory {

    @Override
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls) {
        return listBeansOfExactClass(cls, null, null, null);
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery) {
        return listBeans(cls, optionalQuery, null, null);
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery, Comparator<T> order) {
        List<T> result = listBeans(cls, optionalQuery, null, null);
        Collections.sort(result, order);
        return result;
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls) {
        return listBeans(cls, null, null, null);
    } // listBeans()

} // AbstractBeanFactory
