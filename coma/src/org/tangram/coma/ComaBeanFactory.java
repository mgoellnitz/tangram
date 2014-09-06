/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.coma;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;

@SuppressWarnings("unchecked")
public class ComaBeanFactory extends AbstractComaBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ComaBeanFactory.class);

    @Override
    public Object createBlob(String id, String propertyName, String mimeType, long len, byte[] data) {
        return new ComaBlob(id, propertyName, mimeType, len, data);
    } // createBlob()


    @Override
    public Content createContent(String id, String type, Map<String, Object> properties) {
        return new ComaContent(id, type, properties);
    } // createContent()


    public Set<Content> getChildrenWithType(String parentId, String type) {
        Set<Content> result = new HashSet<>();
        for (String id : getChildrenWithTypeIds(parentId, type)) {
            result.add(getBean(id));
        } // for
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChildrenWithType() size="+result.size());
        } // if
        return result;
    } // getChildrenWithType()


    public Set<Content> getChildren(String startFolderId, String pattern) {
        if (LOG.isInfoEnabled()) {
            LOG.info("getChildren() "+startFolderId);
        } // if
        Set<String> resultIds = getChildrenIds(startFolderId, pattern);
        Set<Content> results = new HashSet<>();
        for (String id : resultIds) {
            results.add(getBean(id));
        } // for
        return results;
    } // getChildren()


    @Override
    public void addListener(Class<? extends Content> arg0, BeanListener arg1) {
        // Since we don't have any changes, we don't need to register listeners
    }

} // ComaBeanFactory
