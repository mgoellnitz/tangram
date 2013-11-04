/**
 *
 * Copyright 2011-2013 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.nucleus;

import org.datanucleus.identity.OID;
import org.datanucleus.identity.OIDImpl;
import org.tangram.content.Content;
import org.tangram.jdo.AbstractJdoBeanFactory;
import org.tangram.mutable.MutableCode;

public class NucleusBeanFactory extends AbstractJdoBeanFactory {

    @Override
    protected Object getObjectId(String internalId, Class<? extends Content> kindClass) {
        OID oid = new OIDImpl(kindClass.getName(), internalId);
        return oid;
    } // getObjectId()


    @Override
    public Class<? extends MutableCode> getCodeClass() {
        return Code.class;
    } // getCodeClass()

} // NucleusBeanFactory
