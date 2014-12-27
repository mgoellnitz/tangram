/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
package org.tangram.components.nucleus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jdo.annotations.PersistenceCapable;
import org.datanucleus.enhancer.DataNucleusEnhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.jdo.JdoBeanFactory;
import org.tangram.logic.ClassRepository;


@Named
@Singleton
public class ClassRepositoryEnhancer implements BeanListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClassRepositoryEnhancer.class);

    @Inject
    private ClassRepository classRepository;

    @Inject
    private JdoBeanFactory beanFactory;


    @Override
    @SuppressWarnings("unchecked")
    public void reset() {
        Map<String, Class<Content>> classes = classRepository.get(Content.class);
        LOG.info("reset() number of classes {}", classes.size());
        Collection<Class<? extends Content>> modelClasses = new HashSet<Class<? extends Content>>();
        for (Class<Content> c : classes.values()) {
            if (c.getAnnotation(PersistenceCapable.class)!=null) {
                LOG.info("reset() defining {}", c.getName());
                try {
                    DataNucleusEnhancer enhancer = new DataNucleusEnhancer();
                    enhancer.setVerbose(true);
                    enhancer.setSystemOut(true);
                    final String classname = c.getName();
                    enhancer.setClassLoader(classRepository.getClassLoader());
                    enhancer.addClass(classname, classRepository.getBytes(classname));
                    int numClasses = enhancer.enhance();
                    if (numClasses>0) {
                        final byte[] enhancedBytes = enhancer.getEnhancedBytes(classname);
                        classRepository.overrideClass(classname, enhancedBytes);
                        final Class<? extends Object> enhancedClass = classRepository.get(classname);
                        modelClasses.add((Class<? extends Content>) enhancedClass);
                    } else {
                        LOG.error("reset() cannot integrate model class "+classname);
                    } // if
                } catch (Throwable e) {
                    LOG.error("reset()", e);
                } // try/catch
            } // if
        } // for
        beanFactory.setAdditionalClasses(modelClasses);
    } // reset()


    @PostConstruct
    public void afterPropertiesSet() {
        classRepository.addListener(this);
        reset();
    } // afterPropertiesSet()

} // ClassRepositoryEnhancer
