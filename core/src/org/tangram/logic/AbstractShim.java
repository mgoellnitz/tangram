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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.logic;

import org.tangram.content.Content;


/**
 *
 * Abstract base class for logic extension classes in the view layer.
 *
 */
public abstract class AbstractShim<T extends Content> implements Shim {

    protected String attributeName;

    protected T delegate;


    public AbstractShim(T delegate) {
        this.attributeName = getClass().getSimpleName();
        this.delegate = delegate;
    } // AbstractViewShim()


    @Override
    public String getAttributeName() {
        return attributeName;
    } // getAttributeName()


    @Override
    public String getId() {
        return delegate.getId();
    } // getDelegate()

} // AbstractViewShim
