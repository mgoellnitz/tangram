/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.view;

public class TargetDescriptor {

    public TargetDescriptor(Object bean, String view, String action) {
        this.bean = bean;
        this.view = view;
        this.action = action;
    } // TargetDescriptor()

    public Object bean;

    public String view;

    public String action;


    public Object getBean() {
        return bean;
    }


    public String getView() {
        return view;
    }


    public String getAction() {
        return action;
    }

} // TargetDescriptor

