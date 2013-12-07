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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.view.velocity;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.tangram.components.CodeResourceCache;


/**
 * This dummy bean statically injects the code resource cache into the velocity resource loader.
 *
 * This is a dangerous hack since it must be called before the spring velocity initialization
 * which depends on the order of bean definitions in the tangram-configurer.xml
 */
@Named
@Singleton
public class VelocityPatchBean {

    @Inject
    public void setCodeResourceCache(CodeResourceCache codeResourceCache) {
        VelocityResourceLoader.codeResourceCache = codeResourceCache;
    }

} // VelocityPatchBean()
