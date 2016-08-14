/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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
package org.tangram.coma;

import java.util.AbstractList;
import java.util.List;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public class LazyContentList extends AbstractList<Content> {

    private final BeanFactory<?> beanFactory;

    private final List<String> idList;


    public LazyContentList(BeanFactory<?> beanFactory, List<String> idList) {
        this.beanFactory = beanFactory;
        this.idList = idList;
    } // LazyContentList()


    @Override
    public Content get(int index) {
        return beanFactory.getBean(idList.get(index));
    } // get()


    @Override
    public int size() {
        return idList.size();
    } // size()

} // LazyContentList
