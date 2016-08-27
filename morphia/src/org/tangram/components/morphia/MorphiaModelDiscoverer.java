/**
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
package org.tangram.components.morphia;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.mongodb.morphia.annotations.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.logic.ClassRepository;
import org.tangram.morphia.MorphiaBeanFactory;


/**
 * Discovers entity implementations in the class repository and adds them to the morphia ORM handling.
 */
@Named
@Singleton
public class MorphiaModelDiscoverer implements BeanListener {

    private static final Logger LOG = LoggerFactory.getLogger(MorphiaModelDiscoverer.class);

    @Inject
    private ClassRepository classRepository;

    @Inject
    private MorphiaBeanFactory beanFactory;


    @Override
    @SuppressWarnings("unchecked")
    public void reset() {
        Map<String, Class<Content>> classes = classRepository.get(Content.class);
        LOG.info("reset() number of classes {}", classes.size());
        Collection<Class<? extends Content>> modelClasses = new HashSet<>();
        for (Class<Content> c : classes.values()) {
            if (c.getAnnotation(Entity.class)!=null) {
                LOG.info("reset() defining {}", c.getName());
                modelClasses.add(c);
            } // if
        } // for
        beanFactory.setAdditionalClasses(modelClasses);
    } // reset()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        classRepository.addListener(this);
        beanFactory.setClassLoader(classRepository.getClassLoader());
    } // afterPropertiesSet()

} // MorphiaModelDiscoverer
