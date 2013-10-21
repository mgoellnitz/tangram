/**
 * 
 * Copyright 2011-2012 Martin Goellnitz
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.tangram.content.AbstractBeanFactory;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public abstract class AbstractComaBeanFactory extends AbstractBeanFactory implements InitializingBean {

    protected static Log log = LogFactory.getLog(AbstractComaBeanFactory.class);

    private Connection dbConnection;

    private String dbUrl;

    private String dbDriver;

    private String dbUser;

    private String dbPassword;

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


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


    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Content createBean(Class cls) {
        // this is a read only implementation
        return null;
    } // createBean()


    public abstract Content createContent(String id, String type, Map<String, Object> properties);


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


    @Override
    public Content getBeanForUpdate(String id) {
        return getBean(id);
    } // getBeanForUpdate()


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> T getBean(Class<T> cls, String id) {
        return (T)getBean(id);
    } // getBeanForUpdate()


    @Override
    public <T extends Content> T getBeanForUpdate(Class<T> cls, String id) {
        return getBean(cls, id);
    } // getBeanForUpdate()


    @Override
    @SuppressWarnings("rawtypes")
    public List<Content> listBeansOfExactClass(Class cls, String optionalQuery, String orderProperty, Boolean ascending) {
        List<Content> result = new ArrayList<Content>();
        String typeName = cls.getSimpleName();
        Set<String> ids = listIds(typeName, optionalQuery, orderProperty, ascending);
        for (String id : ids) {
            result.add(getBean(id));
        } // for
        return result;
    } // listBeans()


    @Override
    @SuppressWarnings("rawtypes")
    public List<Content> listBeans(Class cls, String optionalQuery, String orderProperty, Boolean ascending) {
        return listBeansOfExactClass(cls, optionalQuery, orderProperty, ascending);
    } // listBeans()


    /** supporting methods for implementing CM style access to content **/

    public abstract Object createBlob(String id, String propertyName, String mimeType, long len, byte[] data);


    /**
     * instance "id" must be of type "type"!!!
     * 
     * @param type
     * @param id
     * @return
     */
    public Map<String, Object> getProperties(BeanFactory factory, String type, String id) {
        Map<String, Object> properties = new HashMap<String, Object>();
        if ((type==null)||(type.length()==0)) {
            // it's most likely a folder
            return properties;
        } // if
        String query = null;
        try {
            query = "SELECT * FROM "+type.toLowerCase()+" WHERE id_ = "+id+" ORDER BY version_ DESC";
            Statement s = dbConnection.createStatement();
            ResultSet resultSet = s.executeQuery(query);
            if (resultSet.next()) {
                int contentId = resultSet.getInt("id_");
                int version = resultSet.getInt("version_");
                if (log.isDebugEnabled()) {
                    log.debug("getProperties() "+contentId+"/"+version+": "+type);
                } // if

                ResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 1; i<=metaData.getColumnCount(); i++ ) {
                    String columnName = metaData.getColumnName(i);
                    // if ( !columnName.endsWith("_")) {
                    Object value = resultSet.getObject(i);
                    if (log.isDebugEnabled()) {
                        log.debug("getProperties() property "+columnName+" = "+value);
                    } // if
                    properties.put(columnName, value);
                    // } else {
                    // if (log.isDebugEnabled()) {
                    // log.debug("getProperties() implicit property "+columnName);
                    // } // if
                    // } // if
                } // for

                // select links
                s = dbConnection.createStatement();
                query = "SELECT * FROM linklists WHERE sourcedocument = "+id+" AND sourceversion = "+version
                        +" ORDER BY propertyname ASC, linkindex ASC";
                resultSet = s.executeQuery(query);
                Map<String, List<String>> linkLists = new HashMap<String, List<String>>();
                while (resultSet.next()) {
                    String propertyName = resultSet.getString("propertyname");
                    String targetId = resultSet.getString("targetdocument");
                    int linkIndex = resultSet.getInt("linkindex");
                    if (log.isDebugEnabled()) {
                        log.debug("getProperties() "+propertyName+"["+linkIndex+"] "+targetId);
                    } // if
                    List<String> ids = linkLists.get(propertyName);
                    if (ids==null) {
                        ids = new ArrayList<String>();
                        linkLists.put(propertyName, ids);
                    } // if
                    ids.add(linkIndex, targetId);
                } // while
                for (Entry<String, List<String>> entry : linkLists.entrySet()) {
                    properties.put(entry.getKey(), new LazyContentList(factory, entry.getValue()));
                } // for

                // select blobs
                s = dbConnection.createStatement();
                query = "SELECT * FROM blobs WHERE documentid = "+id+" AND documentversion = "+version+" ORDER BY propertyname ASC";
                resultSet = s.executeQuery(query);
                while (resultSet.next()) {
                    String propertyName = resultSet.getString("propertyname");
                    int blobId = resultSet.getInt("target");

                    Statement st = dbConnection.createStatement();
                    query = "SELECT * FROM blobdata WHERE id = "+blobId;
                    ResultSet blobSet = st.executeQuery(query);
                    if (blobSet.next()) {
                        String mimeType = blobSet.getString("mimetype");
                        byte[] data = blobSet.getBytes("data");
                        long len = blobSet.getLong("len");
                        properties.put(propertyName, createBlob(id, propertyName, mimeType, len, data));
                        if (log.isDebugEnabled()) {
                            log.debug("getProperties() "+propertyName+" blob bytes "+data.length+" ("+len+")");
                        } // if
                    } // if
                } // while

                // select xml
                s = dbConnection.createStatement();
                query = "SELECT * FROM texts WHERE documentid = "+id+" AND documentversion = "+version+" ORDER BY propertyname ASC";
                resultSet = s.executeQuery(query);
                while (resultSet.next()) {
                    String propertyName = resultSet.getString("propertyname");
                    int target = resultSet.getInt("target");
                    // int segment = resultSet.getInt("segment");

                    Statement st = dbConnection.createStatement();
                    query = "SELECT * FROM sgmltext WHERE id = "+target;
                    ResultSet textSet = st.executeQuery(query);
                    StringBuilder text = new StringBuilder();
                    while (textSet.next()) {
                        String xmlText = textSet.getString("text");
                        text.append(xmlText);
                        if (log.isDebugEnabled()) {
                            log.debug("getBean() "+propertyName+" "+textSet.getInt("id")+" "+textSet.getInt("segmentno")+" "+xmlText);
                        } // if
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("getProperties() "+propertyName+" text="+text.toString());
                    } // if

                    Statement sd = dbConnection.createStatement();
                    query = "SELECT * FROM sgmldata WHERE id = "+target;
                    ResultSet dataSet = sd.executeQuery(query);
                    StringBuilder data = new StringBuilder();
                    while (dataSet.next()) {
                        String xmlData = dataSet.getString("data");
                        data.append(xmlData);
                        if (log.isDebugEnabled()) {
                            log.debug("getProperties() "+propertyName+" "+dataSet.getInt("id")+" "+dataSet.getInt("segmentno")+" "
                                    +xmlData);
                        } // if
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("getProperties() "+propertyName+" data="+data.toString());
                    } // if

                    try {
                        properties.put(propertyName, ComaTextConverter.convert(text, data));
                    } catch (Exception e) {
                        log.error("getProperties() ignoring richtext", e);
                        properties.put(propertyName, text.toString());
                    } // try/catch
                } // while
            } // if
        } catch (SQLException se) {
            log.error("getProperties() "+query, se);
        } // try/catch
        return properties;
    } // getProperties()


    public String getType(String id) {
        String type = null;
        try {
            Statement s = dbConnection.createStatement();
            String query = "SELECT * FROM resources WHERE id_ = '"+id+"'";
            ResultSet resultSet = s.executeQuery(query);
            if (resultSet.next()) {
                type = resultSet.getString("documenttype_");
                if (log.isDebugEnabled()) {
                    int contentId = resultSet.getInt("id_");
                    log.debug("getType() "+contentId+": "+type);
                } // if
                if (type==null) {
                    type = ""; // Folder indication
                } // if
            } // if
        } catch (SQLException se) {
            log.error("getType()", se);
        } // try/catch
        return type;
    } // getType()


    public String getChildId(String path) {
        try {
            String[] arcs = path.split("/");
            String currentFolder = "1"; // root
            for (String folder : arcs) {
                if (log.isInfoEnabled()) {
                    log.info("getChildId() lookup up "+folder+" in id "+currentFolder);
                } // if
                if (folder.length()>0) {
                    currentFolder = getChildId(folder, currentFolder);
                } // if
            } // for
            return currentFolder;
        } catch (RuntimeException se) {
            log.error("getChildId()", se);
        } // try/catch
        return null;
    } // getChildId()


    public Set<String> listIds(String typeName, String optionalQuery, String orderProperty, Boolean ascending) {
        Set<String> ids = new HashSet<String>();
        String query = null;
        try {
            query = "SELECT id_ FROM resources WHERE documenttype_ = '"+typeName+"' ";
            if (optionalQuery!=null) {
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
            Statement s = dbConnection.createStatement();
            ResultSet resultSet = s.executeQuery(query);
            while (resultSet.next()) {
                if (log.isInfoEnabled()) {
                    int contentId = resultSet.getInt("id_");
                    ids.add(""+contentId);
                    if (log.isDebugEnabled()) {
                        log.debug("getBean() "+contentId);
                    } // if
                } // if
            } // while
        } catch (SQLException se) {
            log.error("getListBeans() "+query, se);
        } // try/catch
        return ids;
    } // listIds()


    public String getChildId(String name, String parentId) {
        String query = null;
        try {
            query = "SELECT * FROM resources WHERE folderid_ = "+parentId+" AND name_ = '"+name+"'";
            Statement s = dbConnection.createStatement();
            ResultSet resultSet = s.executeQuery(query);
            String id = null;
            if (resultSet.next()) {
                id = ""+resultSet.getInt("id_");
                if (log.isDebugEnabled()) {
                    log.debug("getChildId() "+parentId+"/"+name+": "+id);
                } // if
            } // if
            return id;
        } catch (SQLException se) {
            log.error("getChildId() "+query, se);
        } // try/catch
        return null;
    } // getChildId()


    public Set<String> getChildrenIds(String parentId, String type, String pattern) {
        if (log.isInfoEnabled()) {
            log.info("getChildrenIds() parentId="+parentId+" type="+type+" pattern="+pattern);
        } // if
        Pattern p = null;
        if (pattern!=null) {
            p = Pattern.compile(pattern);
        } // if
        Set<String> result = new HashSet<String>();
        String query = null;
        try {
            Statement s = dbConnection.createStatement();
            query = "SELECT * FROM resources WHERE folderid_ = "+parentId;
            if (type!=null) {
                query += " AND documenttype_ = '"+type+"'";
            } // if
            ResultSet resultSet = s.executeQuery(query);
            String id = null;
            while (resultSet.next()) {
                id = ""+resultSet.getInt("id_");
                String name = resultSet.getString("name_");
                if (log.isInfoEnabled()) {
                    log.info("getChildrenIds() "+parentId+"/"+name+": "+id);
                } // if
                if (p==null) {
                    result.add(id);
                } else {
                    if (p.matcher(name).matches()) {
                        if (log.isInfoEnabled()) {
                            log.info("getChildrenIds() match!");
                        } // if
                        result.add(id);
                    } // if
                } // if
            } // if
            return result;
        } catch (SQLException se) {
            log.error("getChildrenIds() "+query, se);
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
        if (log.isInfoEnabled()) {
            log.info("getReferrerIds() parentId="+targetId+" type="+type+" property="+property);
        } // if
        Set<String> result = new HashSet<String>();
        String query = null;
        try {
            query = "SELECT * FROM linklists WHERE targetdocument = "+targetId;
            if (property!=null) {
                query += " AND propertyname = '"+property+"'";
            } // if
            Statement s = dbConnection.createStatement();
            ResultSet resultSet = s.executeQuery(query);
            while (resultSet.next()) {
                String sourceid = ""+resultSet.getInt("sourcedocument");
                String sourceversion = ""+resultSet.getInt("sourceversion");
                if (log.isInfoEnabled()) {
                    log.info("getReferrerIds() "+sourceid+"/"+sourceversion+"#"+property+" -> "+targetId);
                } // if
                  // TODO: check for latest version
                if ( !result.contains(sourceid)) {
                    result.add(sourceid);
                } // if
            } // if
            return result;
        } catch (SQLException se) {
            log.error("getReferrerIds() "+query, se);
        } // try/catch
        return result;
    } // getReferrerIds()


    @Override
    public void afterPropertiesSet() throws Exception {
        Class.forName(dbDriver).newInstance();
        dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    } // afterPropertiesSet()

} // AbstractComaBeanFactory