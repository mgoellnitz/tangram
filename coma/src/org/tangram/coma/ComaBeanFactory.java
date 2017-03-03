/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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
package org.tangram.coma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.util.SystemUtils;


public class ComaBeanFactory extends AbstractComaBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ComaBeanFactory.class);

    private final Map<Class<? extends Content>, List<BeanListener>> attachedListeners = new HashMap<>();

    @Inject
    private Set<ComaBeanPopulator> populators;

    /**
     * set of document type names holding codes.
     */
    private Set<String> codeTypeNames = new HashSet<>();

    /**
     * Id of the base folder to hold all codes relevant to tangram.
     * This is meant for laege repositories where otherwise too many objects might get queried.
     * Slows things down while keeping the needed amount of memory calculatable.
     */
    private String codeBaseFolderId = "1";


    public void setCodeTypeNames(Set<String> codeTypeNames) {
        this.codeTypeNames = codeTypeNames;
    }


    public void setCodeBaseFolderId(String codeBaseFolderId) {
        this.codeBaseFolderId = codeBaseFolderId;
    }


    @Override
    public Object createBlob(String id, String propertyName, String mimeType, long len, byte[] data) {
        return new ComaBlob(id, propertyName, mimeType, len, data);
    } // createBlob()


    @Override
    public Content createContent(String id, String type, Map<String, Object> properties) {
        return new ComaContent(id, type, properties);
    } // createContent()


    public void collectSubFoldersRecursive(Collection<String> collectedIds, String folder) {
        Set<String> folderIds = listIds(null, "folderid_="+folder, null, false);
        collectedIds.addAll(folderIds);
        for (String id : folderIds) {
            collectSubFoldersRecursive(collectedIds, id);
        } // for
    } // collectSubFoldersRecursive()


    public <T extends Content> List<T> listCode() {
        Collection<String> folders = new HashSet<>();
        collectSubFoldersRecursive(folders, codeBaseFolderId);
        LOG.info("listCode() folders={}", folders);
        List<T> result = new ArrayList<>();
        for (String folder : folders) {
            for (String typeName : codeTypeNames) {
                for (String id : listIds(typeName, "folderid_ = "+folder, null, false)) {
                    ComaCode code = new ComaCode(id, typeName, getProperties(this, typeName, id));
                    for (ComaBeanPopulator populator : populators) {
                        populator.populate(code);
                    } // if
                    if (code.containsKey("annotation")&&code.containsKey("mimeType")) {
                        result.add(SystemUtils.convert(code));
                    } // if
                } // for
            } // for
        } // for
        return result;
    } // listCode()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls) {
        return cls==CodeResource.class ? listCode() : super.listBeans(cls);
    } // listBeans()


    public Set<Content> getChildrenWithType(String parentId, String type) {
        Set<Content> result = new HashSet<>();
        for (String id : getChildrenWithTypeIds(parentId, type)) {
            result.add(getBean(id));
        } // for
        LOG.debug("getChildrenWithType() size={}", result.size());
        return result;
    } // getChildrenWithType()


    public Set<Content> getChildren(String startFolderId, String pattern) {
        LOG.info("getChildren() {}", startFolderId);
        Set<String> resultIds = getChildrenIds(startFolderId, pattern);
        Set<Content> results = new HashSet<>();
        for (String id : resultIds) {
            results.add(getBean(id));
        } // for
        return results;
    } // getChildren()


    /**
     * Attach a listener for any changes dealing with classes of the given type.
     * Coma is read only and does not even deal with external changes so far.
     *
     * @param cls class to be notified when instances of that class have been changed
     * @param listener listener to be notified about changes
     */
    @Override
    public void addListener(Class<? extends Content> cls, BeanListener listener) {
        synchronized (attachedListeners) {
            List<BeanListener> listeners = attachedListeners.get(cls);
            if (listeners==null) {
                listeners = new ArrayList<>();
                attachedListeners.put(cls, listeners);
            } // if
            listeners.add(listener);
        } // synchronized
        listener.reset();
        LOG.info("addListener() {}: {}", cls.getSimpleName(), attachedListeners.get(cls).size());
    } // addListener()

} // ComaBeanFactory
