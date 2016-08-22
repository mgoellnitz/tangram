/**
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.morphia.test.content;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import org.tangram.content.BeanFactory;
import org.tangram.mutable.test.content.SubInterface;


/**
 * Persistent bean sub class just for test purposes.
 */
@Entity
public class SubClass extends BaseClass implements SubInterface {

    private String subtitle;

    @Transient
    private BeanFactory<?> beanFactory;


    @Override
    public String getSubtitle() {
        return subtitle;
    }


    @Override
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }


    @Override
    public BeanFactory<?> getBeanFactory() {
        return beanFactory;
    }


    @Override
    public void setBeanFactory(BeanFactory<?> beanFactory) {
        this.beanFactory = beanFactory;
    }

} // SubClass
