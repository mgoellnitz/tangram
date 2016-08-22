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
package org.tangram.components.coma.test;

import java.util.Date;
import javax.inject.Named;
import javax.inject.Singleton;
import org.tangram.coma.ComaBeanPopulator;
import org.tangram.coma.ComaContent;


/**
 * Dummy population of beans as code.
 */
@Named
@Singleton
public class ComaTestCodeBeanPopulator implements ComaBeanPopulator {

    @Override
    public void populate(ComaContent content) {
        if ("Topic".equals(content.getDocumentType())) {
            content.put("annotation", "org.tangram.example.Topic");
            content.put("mimeType", "text/html");
            content.put("code", "<html><head><title>Test</title></head><body></body></html>");
            content.put("modificationTime", new Date().getTime());
        } // if
    } // populate()

} // ComaTestCodeBeanPopulator
