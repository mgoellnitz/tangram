/*
 *
 * Copyright 2015-2016 Martin Goellnitz
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

import com.github.rjeschke.txtmark.Processor;


/**
 * Markdown wrapper class to indicate that the contents should be rendered from markdown to markup before output.
 */
public class Markdown {

    final private char[] content;


    public Markdown(char[] content) {
        this.content = content;
    }


    public char[] getContent() {
        return content;
    }


    @Override
    public String toString() {
        return content==null ? "" : new String(content);
    } // toString()


    public final char[] getMarkup() {
        return Processor.process(toString()).toCharArray();
    } // getMarkup()


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Markdown) ? toString().equals(obj.toString()) : false;
    } // equals()

} // Markdown
