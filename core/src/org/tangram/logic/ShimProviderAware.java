/*
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
package org.tangram.logic;


/**
 * Indicator interface to show that implementing instances need a shim provider.
 */
public interface ShimProviderAware {

    /**
     * Set the shim provider to be used by implementing instances.
     *
     * @param shimProvider shim provider instance
     */
    void setShimProvider(ShimProvider shimProvider);

} // ShimProviderAware
