/**
 *
 * Copyright 2014-2017 Martin Goellnitz
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
package org.tangram.ebean.test.content;

import io.ebean.Query;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import org.tangram.content.BeanFactory;
import org.tangram.mutable.test.content.SubInterface;


/**
 * Persistent bean sub class just for test purposes.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorValue("SubClass")
public class SubClass extends BaseClass implements SubInterface<Query<?>> {

    private String subtitle;

    @Transient
    private BeanFactory<Query<?>> beanFactory;


    @Override
    public String getSubtitle() {
        return subtitle;
    }


    @Override
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }


    @Override
    public BeanFactory<Query<?>> getBeanFactory() {
        return beanFactory;
    }


    @Override
    public void setBeanFactory(BeanFactory<Query<?>> beanFactory) {
        this.beanFactory = beanFactory;
    }

} // SubClass
