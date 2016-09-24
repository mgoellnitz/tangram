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
package org.tangram.content;

/**
 * Instances need a "setBeanFactory()" call.
 *
 * This is used for codes in the code repository since those don't get automatic dependency injection.
 */
public interface BeanFactoryAware<Q extends Object> {

    /**
     * attach bean factory to implementing instance.
     *
     * @param factory bean factory instance to attach
     */
    void setBeanFactory(BeanFactory<Q> factory);

} // BeanFactoryAware
