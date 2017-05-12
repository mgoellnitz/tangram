/**
 *
 * Copyright 2016-2017 Martin Goellnitz
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
package org.tangram.morphia;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.ObjectFactory;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.mapping.DefaultCreator;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.Content;
import org.tangram.mutable.AbstractMutableBeanFactory;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.util.ClassResolver;
import org.tangram.util.SystemUtils;


@Named("beanFactory")
@Singleton
public class MorphiaBeanFactory extends AbstractMutableBeanFactory<Datastore, Query<?>> implements MutableBeanFactory<Datastore, Query<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(MorphiaBeanFactory.class);

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private Datastore datastore;

    protected List<Class<? extends Content>> allClasses = null;

    private Collection<Class<? extends Content>> additionalClasses = Collections.emptySet();

    private String mongoUri = "mongodb://localhost:27017/tangram";

    private String mongoDB = "tangram";


    public void setUri(String mongoUri) {
        this.mongoUri = mongoUri;
    }


    public void setDatabase(String mongoDB) {
        this.mongoDB = mongoDB;
    }


    @Override
    public <T extends Content> T getBean(Class<T> cls, String kind, String internalId) throws Exception {
        if (modelClasses==null) {
            getClasses();
        } // if
        Class<? extends Content> kindClass = tableNameMapping.get(kind);
        if (kindClass==null) {
            throw new Exception("Passed over kind "+kind+" not valid");
        } // if
        if (!(cls.isAssignableFrom(kindClass))) {
            throw new Exception("Passed over class "+cls.getSimpleName()+" does not match "+kindClass.getSimpleName());
        } // if
        LOG.info("getBean() {}:{}", kindClass.getName(), internalId);
        return convert(cls, datastore.get(kindClass, new ObjectId(internalId)));
    } // getBean()


    @Override
    public MorphiaContent getBean(String id) {
        return getBean(MorphiaContent.class, id);
    } // getBean()


    @Override
    public Class<? extends Content> getBaseClass() {
        return MorphiaContent.class;
    } // getBaseClass()


    @Override
    public void beginTransaction() {
        //
    } // beginTransaction()


    @Override
    public void commitTransaction() {
        //
    } // commitTransaction()


    @Override
    public void rollbackTransaction() {
        //
    } // rollbackTransaction()


    @Override
    protected boolean hasManager() {
        return true;
    } // hasManager()


    @Override
    protected <T extends Content> void apiPersist(T bean) {
        datastore.save(bean);
    } // apiPersist()


    @Override
    protected <T extends Content> void apiDelete(T bean) {
        datastore.delete(bean);
    } // apiDelete()


    @Override
    public Query<?> createQuery(Class<? extends Content> cls, String expression) {
        Query<? extends Content> query = datastore.createQuery(cls);
        if (StringUtils.isNotEmpty(expression)) {
            query.where(expression);
        } // if
        return query;
    } // createQuery()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> c, String query, String orderProperty, Boolean ascending) {
        List<T> results = new ArrayList<>();
        try {
            for (Class<T> cls : getImplementingClasses(c)) {
                String shortTypeName = cls.getSimpleName();
                Query<T> q = datastore.createQuery(cls);
                if (StringUtils.isNotEmpty(query)) {
                    q.where(query);
                } // if
                if (orderProperty!=null) {
                    q = q.order((ascending ? "" : "-")+orderProperty);
                } // if
                // Default is no ordering - not even via IDs
                LOG.info("listBeans() looking up instances of {}{}", shortTypeName, (q==null ? "" : " with condition "+q));
                LOG.info("listBeans() morphia query object is {} ", q);
                results.addAll(SystemUtils.convert(q.asList()));
                injectBeanFactory(results);
            } // for
            LOG.info("listBeans() looked up {} raw entries", results.size());
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeans() query ", e);
        } // try/catch/finally
        return results;
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeans(Query<?> query) {
        List<T> result = new ArrayList<>();
        try {
            String shortTypeName = query.getEntityClass().getSimpleName();
            LOG.info("listBeans() looking up instances of {}{}", shortTypeName, (query==null ? "" : " with condition "+query));
            LOG.info("listBeans() morphia query object is {} ", query);
            result.addAll(SystemUtils.convert(query.asList()));
            injectBeanFactory(result);
            LOG.info("listBeans() looked up {} raw entries", result.size());
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeans() query ", e);
        } // try/catch/finally
        return result;
    } // listBeans()


    @Override
    public Collection<Class<? extends Content>> getAllClasses() {
        synchronized (this) {
            if (allClasses==null) {
                allClasses = new ArrayList<>();
                try {
                    List<String> classNames = SystemUtils.convert(startupCache.get(getClassNamesCacheKey(), List.class));
                    if (classNames==null) {
                        ClassResolver resolver = new ClassResolver(getBasePackages());
                        classNames = new ArrayList<>();
                        Set<Class<? extends Content>> resolvedClasses = new HashSet<>();
                        resolvedClasses.addAll(resolver.getAnnotatedSubclasses(MorphiaContent.class, Entity.class));
                        resolvedClasses.addAll(resolver.getSubclasses(Content.class));
                        for (Class<? extends Content> cls : resolvedClasses) {
                            LOG.info("getAllClasses() * {}", cls.getName());
                            if (!allClasses.contains(cls)) {
                                classNames.add(cls.getName());
                                allClasses.add(cls);
                            } // if
                        } // for
                        LOG.info("getAllClasses() # class names {}", classNames.size());
                        startupCache.put(getClassNamesCacheKey(), classNames);
                    } else {
                        // re-fill runtimes caches from persistence startup cache
                        for (String beanClassName : classNames) {
                            Class<? extends Content> cls = SystemUtils.convert(Class.forName(beanClassName));
                            LOG.info("getAllClasses() # {}", cls.getName());
                            allClasses.add(cls);
                        } // for
                    } // if
                    allClasses.addAll(additionalClasses);
                } catch (Exception e) {
                    LOG.error("getAllClasses() outer", e);
                } // try/catch
            } // if
        } // synchronized
        return allClasses;
    } // getAllClasses()


    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    } // setClassLoader()


    /**
     * Default creator implementation referring to a a given class loader.
     */
    private class TangramCreator extends DefaultCreator {

        private final ClassLoader classLoader;


        public TangramCreator(ClassLoader cl) {
            classLoader = cl;
        }


        @Override
        protected ClassLoader getClassLoaderForClass() {
            return classLoader;
        }

    }


    private void init() {
        ObjectFactory factory = new TangramCreator(classLoader);
        MapperOptions options = new MapperOptions();
        options.setObjectFactory(factory);
        Morphia morphia = new Morphia();
        for (Class<?> c : getClasses()) {
            LOG.info("init() class {}", c.getName());
            morphia.map(c);
        } // for
        LOG.info("init() access: {} database: {}", mongoUri, mongoDB);
        MongoClientURI uri = new MongoClientURI(mongoUri);
        MongoClient mongoClient = new MongoClient(uri);
        datastore = morphia.createDatastore(mongoClient, mongoDB);
        datastore.ensureIndexes();
    } // init()


    private void reset() {
        cache = new HashMap<>();
        queryCache = new HashMap<>();
        implementingClassesMap = null;
        modelClasses = null;
        init();
    } // reset()


    public void setAdditionalClasses(Collection<Class<? extends Content>> classes) {
        Set<Class<? extends Content>> classSet = new HashSet<>();
        if (classes!=null) {
            for (Class<? extends Content> cls : classes) {
                if ((getBaseClass().isAssignableFrom(cls))&&(cls.getAnnotation(Entity.class)!=null)) {
                    LOG.info("setAdditionalClasses() additional class {}", cls.getSimpleName());
                    classSet.add(cls);
                } // if
            } // for
        } // if
        synchronized (this) {
            additionalClasses = classSet;
            allClasses = null;
            modelClasses = null;
        } // synchronized
        reset();
    } // setAdditionalClasses()


    @Override
    public <F extends Content> String getFilterQuery(Class<F> cls, String filterProperty, String filterValues) {
        return "this."+filterProperty+".indexOf('"+filterValues+"') >= 0";
    } // getFilterQuery()


    @Override
    public Datastore getManager() {
        return datastore;
    } // getManager()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        init();
        Map<String, List<String>> c = SystemUtils.convert(startupCache.get(QUERY_CACHE_KEY, queryCache.getClass()));
        if (c!=null) {
            queryCache = c;
        } // if
    } // afterPropertiesSet()

} // MorphiaBeanFactory
