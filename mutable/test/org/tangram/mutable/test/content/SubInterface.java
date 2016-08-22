/**
 *
 * Copyright 2014 Martin Goellnitz
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
package org.tangram.mutable.test.content;

import org.tangram.content.BeanFactory;
import org.tangram.content.BeanFactoryAware;


/**
 * Persistent bean sub class just for test purposes.
 */
public interface SubInterface extends BaseInterface, BeanFactoryAware {

    String getSubtitle();


    void setSubtitle(String subtitle);


    BeanFactory<?> getBeanFactory();


    void setBeanFactory(BeanFactory<?> beanFactory);

} // SubInterface
