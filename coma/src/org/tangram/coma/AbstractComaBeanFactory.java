/**
 *
 * Copyright 2011-2016 Martin Goellnitz
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.AbstractBeanFactory;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.util.SystemUtils;


public abstract class AbstractComaBeanFactory extends AbstractBeanFactory<StringBuilder> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractComaBeanFactory.class);

    /**
     * describe which type are derived from which others - via documenttype definitions.
     */
    private Map<String, String> parents = new HashMap<>();

    private Connection dbConnection;

    private String dbUrl;

    private String dbDriver;

    private String dbUser;

    private String dbPassword;

    private Map<String, Object> additionalProperties = new HashMap<>();


    /**
     * Obtain mapping from document type name to its parent's document type name.
     *
     * @return Map type to parent map
     */
    public Map<String, String> getParents() {
        return parents;
    }


    /**
     * Set mapping from document type name to its parent's document type name.
     * Must resemble the document type relationship of the underlying CMS repository DB.
     *
     * @param parents map pointing from a document type name to the parents document type name
     */
    public void setParents(Map<String, String> parents) {
        this.parents = parents;
    }


    public String getDbUrl() {
        return dbUrl;
    }


    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }


    public String getDbDriver() {
        return dbDriver;
    }


    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }


    public String getDbUser() {
        return dbUser;
    }


    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }


    public String getDbPassword() {
        return dbPassword;
    }


    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }


    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }


    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }


    public abstract Content createContent(String id, String type, Map<String, Object> properties);


    /**
     * @see BeanFactory#getBean(java.lang.String)
     */
    @Override
    public Content getBean(String id) {
        Content result = null;

        String type = getType(id);
        if (type!=null) {
            Map<String, Object> properties = getProperties(this, type, id);
            properties.putAll(additionalProperties);
            result = createContent(id, type, properties);
        } // if

        return result;
    } // getBean()


    /**
     * Get a bean of a given type with a given id.
     *
     * The resulting bean must adhere to both conditions: id and type.
     *
     * @param cls class of the bean to obtain
     * @param <T> type constraint for the above class
     * @param id id of the bean to obtain
     * @return bean with the given ID and type or null otherwise
     */
    @Override
    public <T extends Content> T getBean(Class<T> cls, String id) {
        return SystemUtils.convert(getBean(id));
    } // getBeanForUpdate()


    /**
     * Create a underlying storage system specific query object.
     *
     * @param cls class of the elements the query should return
     * @param expression optional expression which might be ignored by he underlying system
     * @return new query instance
     */
    @Override
    public StringBuilder createQuery(Class<? extends Content> cls, String expression) {
        return new StringBuilder(expression);
    } // createQuery()


    /**
     * List beans from the repository.
     *
     * @param cls beans class to query for
     * @param <T> type constraint for the above class
     * @param optionalQuery query string specific to the underlying storage system - may be null
     * @param orderProperty name of a attribute of the bean to be used for ascending ordering
     * @param ascending sort ascending or not (descending)
     * @return List of beans adhering the conditions - maybe empty but not null
     */
    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String optionalQuery, String orderProperty, Boolean ascending) {
        return listBeansOfExactClass(cls, optionalQuery, orderProperty, ascending);
    } // listBeans()


    /**
     * List beans from the repository by means of the underlying storage solution.
     *
     * @param query implementation specific query instance
     * @param <T> Type constraint for the elements of the result list
     * @return List of all beans of the given class - maybe empty but not null
     */
    @Override
    public <T extends Content> List<T> listBeans(StringBuilder query) {
        return SystemUtils.convert(listBeans(Content.class, query.toString()));
    } // listBeans()


    /**
     * List beans from the repository of an exact type. Does not take sublcasses into account.
     *
     * @param cls Type requirement to be met.
     * @param <T> Type constraint for the elements of the result list
     * @param query query according to the underlying storage layer - may be null
     * @param orderProperty the resulting list may be ordered according to this property
     * @param ascending sort following orderProperty ascending if true or descending if false - may be null
     * @return list of content elements of exactly the given type T.
     */
    @Override
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String query, String orderProperty, Boolean ascending) {
        List<T> result = new ArrayList<>();
        String typeName = cls.getSimpleName();
        if (parents.keySet().contains(typeName)) {
            for (String id : listIds(typeName, query, orderProperty, ascending)) {
                result.add(convert(cls, getBean(id)));
            } // for
        } // if
        return result;
    } // listBeansOfExactClass()


    /**
     * supporting methods for implementing CM style access to content
     */
    /**
     * Create a transient blob object from content instance.
     *
     * @param id id of the content
     * @param propertyName name of the property to find the blob in
     * @param mimeType mimetype to choose
     * @param len length in bytes
     * @param data blob data
     * @return return abstrace representation of the blob
     */
    public abstract Object createBlob(String id, String propertyName, String mimeType, long len, byte[] data);


    /**
     * Get the properties for an object with a given type and id.
     *
     * The instance "id" must be of type "type" otherwise the method will fail!
     *
     * @param factory should be this bean - erm
     * @param type document type name to fetch properties map for
     * @param id id of the content to fetch properties map for
     * @return map mapping the property names to their respective values
     */
    public Map<String, Object> getProperties(BeanFactory<StringBuilder> factory, String type, String id) {
        Map<String, Object> properties = new HashMap<>();
        if ((type==null)||(type.length()==0)) {
            // it's most likely a folder
            return properties;
        } // if
        String query = "SELECT * FROM "+type+" WHERE id_ = "+id+" ORDER BY version_ DESC";
        try (Statement baseStatement = dbConnection.createStatement(); ResultSet baseSet = baseStatement.executeQuery(query)) {
            if (baseSet.next()) {
                int contentId = baseSet.getInt("id_");
                int version = baseSet.getInt("version_");
                LOG.debug("getProperties() {}/{}: {}", contentId, version, type);

                ResultSetMetaData metaData = baseSet.getMetaData();
                for (int i = 1; i<=metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i).toLowerCase();
                    Object value = baseSet.getObject(i);
                    LOG.debug("getProperties() property {} = {}", columnName, value);
                    properties.put(columnName, value);
                } // for

                // select links
                query = "SELECT * FROM LinkLists WHERE sourcedocument = "+id+" AND sourceversion = "+version
                        +" ORDER BY propertyname ASC, linkindex ASC";
                Map<String, List<String>> linkLists = new HashMap<>();
                try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
                    while (resultSet.next()) {
                        String propertyName = resultSet.getString("propertyname");
                        String targetId = resultSet.getString("targetdocument");
                        int linkIndex = resultSet.getInt("linkindex");
                        LOG.debug("getProperties() {}[{}] {}", propertyName, linkIndex, targetId);
                        List<String> ids = linkLists.get(propertyName);
                        if (ids==null) {
                            ids = new ArrayList<>();
                            linkLists.put(propertyName, ids);
                        } // if
                        ids.add(linkIndex, targetId);
                    } // while
                } catch (SQLException se) {
                    LOG.error("getProperties() "+query, se);
                } // try/catch
                for (Entry<String, List<String>> entry : linkLists.entrySet()) {
                    properties.put(entry.getKey(), new LazyContentList(factory, entry.getValue()));
                } // for

                // select blobs
                query = "SELECT * FROM Blobs WHERE documentid = "+id+" AND documentversion = "+version+" ORDER BY propertyname ASC";
                List<Integer> ids = new ArrayList<>();
                List<String> propertyNames = new ArrayList<>();
                try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
                    while (resultSet.next()) {
                        String propertyName = resultSet.getString("propertyname");
                        int blobId = resultSet.getInt("target");
                        ids.add(blobId);
                        propertyNames.add(propertyName);
                    } // while
                } catch (SQLException se) {
                    LOG.error("getProperties() "+query, se);
                } // try/catch

                for (int i = 0; i<ids.size(); i++) {
                    int blobId = ids.get(i);
                    String propertyName = propertyNames.get(i);
                    query = "SELECT * FROM BlobData WHERE id = "+blobId;
                    try (Statement st = dbConnection.createStatement(); ResultSet blobSet = st.executeQuery(query)) {
                        if (blobSet.next()) {
                            String mimeType = blobSet.getString("mimetype");
                            byte[] data = blobSet.getBytes("data");
                            long len = blobSet.getLong("len");
                            properties.put(propertyName, createBlob(id, propertyName, mimeType, len, data));
                            LOG.debug("getProperties() {} blob bytes {} ({})", propertyName, data.length, len);
                        } // if
                    } catch (SQLException se) {
                        LOG.error("getProperties() "+query, se);
                    } // try/catch
                } // for

                // select xml
                query = "SELECT * FROM Texts WHERE documentid = "+id+" AND documentversion = "+version+" ORDER BY propertyname ASC";
                try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
                    while (resultSet.next()) {
                        String propertyName = resultSet.getString("propertyname");
                        int target = resultSet.getInt("target");
                        // int segment = resultSet.getInt("segment");

                        StringBuilder text = new StringBuilder(256);
                        query = "SELECT * FROM SgmlText WHERE id = "+target;
                        try (Statement st = dbConnection.createStatement(); ResultSet textSet = st.executeQuery(query)) {
                            while (textSet.next()) {
                                String xmlText = textSet.getString("text");
                                text.append(xmlText);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("getBean() "+propertyName+" "+textSet.getInt("id")+" "+textSet.getInt("segmentno")+" "+xmlText);
                                } // if
                            } // if
                        } catch (SQLException se) {
                            LOG.error("getProperties() "+query, se);
                        } // try/catch
                        LOG.debug("getProperties() {} text={}", propertyName, text.toString());

                        query = "SELECT * FROM SgmlData WHERE id = "+target;
                        StringBuilder data = new StringBuilder(256);
                        try (Statement sd = dbConnection.createStatement(); ResultSet dataSet = sd.executeQuery(query)) {
                            while (dataSet.next()) {
                                String xmlData = dataSet.getString("data");
                                data.append(xmlData);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("getProperties() "+propertyName+" "+dataSet.getInt("id")+" "+dataSet.getInt("segmentno")+" "
                                            +xmlData);
                                } // if
                            } // if
                        } catch (SQLException se) {
                            LOG.error("getProperties() "+query, se);
                        } // try/catch
                        LOG.debug("getProperties() {} data={}", propertyName, data.toString());

                        try {
                            properties.put(propertyName, ComaTextConverter.convert(text, data));
                        } catch (Exception e) {
                            LOG.error("getProperties() ignoring richtext", e);
                            properties.put(propertyName, text.toString());
                        } // try/catch
                    } // while
                } catch (SQLException se) {
                    LOG.error("getProperties() "+query, se);
                } // try/catch
            } // if
        } catch (SQLException se) {
            LOG.error("getProperties() "+query, se);
        } // try/catch
        return properties;
    } // getProperties()


    /**
     * Obtain document type name for a given content item.
     *
     * @param id id of the content item
     * @return document type name for the given id from the underlying repository database
     */
    public String getType(String id) {
        String type = null;
        String query = "SELECT * FROM Resources WHERE id_ = '"+id+"'";
        try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
            if (resultSet.next()) {
                type = resultSet.getString("documenttype_");
                if (LOG.isDebugEnabled()) {
                    int contentId = resultSet.getInt("id_");
                    LOG.debug("getType() "+contentId+": "+type);
                } // if
                if (type==null) {
                    type = ""; // Folder indication
                } // if
            } // if
        } catch (SQLException se) {
            LOG.error("getType() "+id, se);
        } // try/catch
        return type;
    } // getType()


    /**
     * Return the ID of a content items described by its path.
     *
     * @param path path to lookup content item from
     * @return id for the given path from the underlying repository database
     */
    public String getChildId(String path) {
        try {
            String[] arcs = path.split("/");
            String currentFolder = "1"; // root
            for (String folder : arcs) {
                LOG.info("getChildId() lookup up {} in id {}", folder, currentFolder);
                if (folder.length()>0) {
                    currentFolder = getChildId(folder, currentFolder);
                } // if
            } // for
            return currentFolder;
        } catch (RuntimeException se) {
            LOG.error("getChildId() "+path, se);
        } // try/catch
        return null;
    } // getChildId()


    public Set<String> listIds(String typeName, String optionalQuery, String orderProperty, Boolean ascending) {
        Set<String> ids = new HashSet<>();
        String query = "SELECT id_ FROM Resources WHERE documenttype_ ";
        query += (typeName==null) ? "IS NULL " : "= '"+typeName+"' ";
        if (optionalQuery!=null) {
            query += "AND ";
            query += optionalQuery;
        } // if
        if (orderProperty!=null) {
            String asc = "ASC";
            if (ascending!=null) {
                asc = ascending ? "ASC" : "DESC";
            } // if
            String order = orderProperty+" "+asc;
            query += " ORDER BY "+order;
        } // if
        LOG.debug("listIds() {}", query);
        try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
            while (resultSet.next()) {
                int contentId = resultSet.getInt("id_");
                ids.add(""+contentId);
                LOG.debug("getBean() {}", contentId);
            } // while
        } catch (SQLException se) {
            LOG.error("listIds() "+query, se);
        } // try/catch
        return ids;
    } // listIds()


    public String getParentId(String childId) {
        String id = null;
        String query = "SELECT * FROM Resources WHERE id_ = "+childId;
        try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
            if (resultSet.next()) {
                id = ""+resultSet.getInt("folderid_");
                LOG.debug("getChildId() {}: {}", childId, id);
            } // if
        } catch (SQLException se) {
            LOG.error("getChildId() "+query, se);
        } // try/catch
        return id;
    } // getParentId()


    public String getChildId(String name, String parentId) {
        String id = null;
        String query = "SELECT * FROM Resources WHERE folderid_ = "+parentId+" AND name_ = '"+name+"'";
        try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
            if (resultSet.next()) {
                id = ""+resultSet.getInt("id_");
                LOG.debug("getChildId() {}/{}: {}", parentId, name, id);
            } // if
        } catch (SQLException se) {
            LOG.error("getChildId() "+query, se);
        } // try/catch
        return id;
    } // getChildId()


    public Set<String> getChildrenIds(String parentId, String type, String pattern) {
        LOG.info("getChildrenIds() parentId={} type={} pattern={}", parentId, type, pattern);
        Pattern p = null;
        if (pattern!=null) {
            p = Pattern.compile(pattern);
        } // if
        Set<String> result = new HashSet<>();
        String query = "SELECT * FROM Resources WHERE folderid_ = "+parentId;
        if (type!=null) {
            query += " AND documenttype_ ";
            query += (type==null) ? "IS NULL" : "= '"+type+"'";
        } // if
        try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
            while (resultSet.next()) {
                String id = ""+resultSet.getInt("id_");
                String name = resultSet.getString("name_");
                LOG.info("getChildrenIds() {}/{}: {}", parentId, name, id);
                if (p==null) {
                    result.add(id);
                } else {
                    if (p.matcher(name).matches()) {
                        LOG.info("getChildrenIds() match!");
                        result.add(id);
                    } // if
                } // if
            } // if
            return result;
        } catch (SQLException se) {
            LOG.error("getChildrenIds() "+query, se);
        } // try/catch
        return result;
    } // getChildrenIds()


    public Set<String> getChildrenIds(String parentId, String pattern) {
        return getChildrenIds(parentId, null, pattern);
    } // getChildrenIds()


    public Set<String> getChildrenIds(String parentId) {
        return getChildrenIds(parentId, null);
    } // getChildrenIds()


    public Set<String> getChildrenWithTypeIds(String parentId, String type) {
        return getChildrenIds(parentId, type, null);
    } // getChildrenWithTypeIds()


    public Set<String> getReferrerIds(String targetId, String type, String property) {
        LOG.info("getReferrerIds() targetId={} type={} property={}", targetId, type, property);
        Set<String> result = new HashSet<>();
        String query = "SELECT * FROM LinkLists WHERE targetdocument = "+targetId;
        if (property!=null) {
            query += " AND propertyname = '"+property+"'";
        } // if
        try (Statement s = dbConnection.createStatement(); ResultSet resultSet = s.executeQuery(query)) {
            while (resultSet.next()) {
                String sourceId = ""+resultSet.getInt("sourcedocument");
                String sourceVersion = ""+resultSet.getInt("sourceversion");
                LOG.info("getReferrerIds() {}/{}/#{} -> {}", sourceId, sourceVersion, property, targetId);
                // Check for latest version referencing the object
                final String sourceType = getType(sourceId);
                final Map<String, Object> properties = getProperties(this, sourceType, sourceId);
                Object version = properties.get("version_");
                if (LOG.isInfoEnabled()) {
                    LOG.info("getReferrerIds() version="+version+" :"+(version!=null ? version.getClass().getName() : "null"));
                } // if
                if ((!result.contains(sourceId))&&sourceVersion.equals(version.toString())) {
                    result.add(sourceId);
                } // if
            } // if
        } catch (SQLException se) {
            LOG.error("getReferrerIds() "+query, se);
        } // try/catch
        return result;
    } // getReferrerIds()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        try {
            Class.forName(dbDriver).newInstance();
        } catch (RuntimeException|ClassNotFoundException|InstantiationException|IllegalAccessException ex) {
            LOG.error("afterPropertiesSet() error loading driver "+dbDriver+"("+this+")", ex);
        } // try/catch
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (RuntimeException|SQLException ex) {
            LOG.error("afterPropertiesSet() error getting connection to "+dbUrl+" as "+dbUser, ex);
        } // try/catch
    } // afterPropertiesSet()

} // AbstractComaBeanFactory
