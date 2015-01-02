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

package org.tangram.components.dinistiq;

import javax.inject.Named;
import javax.inject.Singleton;
import org.tangram.servlet.MeasureTimeFilter;


/**
 * This is exactly the same filter as in the base package except for the annotations.
 */
@Named("measureTimeFilter")
@Singleton
public class TangramMeasureTimeFilter extends MeasureTimeFilter {
    
} // TangramMeasureTimeFilter
