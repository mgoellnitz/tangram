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
package org.tangram.view.velocity;

import javax.inject.Inject;
import org.springframework.beans.factory.InitializingBean;
import org.tangram.components.spring.CodeResourceCache;

// TODO: Since velocity now is a core feature, why is the so add-on style
// TODO: Do this in a spring independent way
public class VelocityPatchBean implements InitializingBean {

    @Inject
    CodeResourceCache codeResourcdCache;


    @Override
    public void afterPropertiesSet() throws Exception {
        VelocityResourceLoader.codeResourceCache = codeResourcdCache;
    } // afterPropertiesSet()

} // VelocityPatchBean()
