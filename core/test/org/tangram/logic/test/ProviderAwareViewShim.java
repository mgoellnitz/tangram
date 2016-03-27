/*
 *
 * Copyright 2015 Martin Goellnitz
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
package org.tangram.logic.test;

import javax.servlet.http.HttpServletRequest;
import org.tangram.logic.ShimProvider;
import org.tangram.logic.ShimProviderAware;
import org.tangram.mock.content.MockContent;


/**
 * Minimalistic mock view shim for mock bean class.
 */
public class ProviderAwareViewShim extends ViewShim implements ShimProviderAware {

    private ShimProvider shimProvider;


    public ProviderAwareViewShim(HttpServletRequest request, MockContent delegate) {
        super(request, delegate);
    } // ViewShim


    public ShimProvider getShimProvider() {
        return shimProvider;
    }


    @Override
    public void setShimProvider(ShimProvider shimProvider) {
        this.shimProvider = shimProvider;
    }

} // ViewShim
