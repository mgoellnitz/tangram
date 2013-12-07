/**
 *
 * Copyright 2013 Martin Goellnitz
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.datanucleus.enhancer.DataNucleusEnhancer;
import org.tangram.content.BeanListener;
import org.tangram.jdo.JdoBeanFactory;
import org.tangram.logic.ClassRepository;
import org.tangram.mutable.MutableContent;


@Named
@Singleton
public class ClassRepositoryEnhancer implements BeanListener {

    private static final Log log = LogFactory.getLog(ClassRepositoryEnhancer.class);

    @Inject
    private ClassRepository classRepository;

    @Inject
    private JdoBeanFactory beanFactory;


    @Override
    @SuppressWarnings("unchecked")
    public void reset() {
        Map<String, Class<MutableContent>> classes = classRepository.get(MutableContent.class);
        if (log.isInfoEnabled()) {
            log.info("reset() number of classes "+classes.size());
        } // if
        Collection<Class<? extends MutableContent>> modelClasses = new HashSet<Class<? extends MutableContent>>();
        for (Class<MutableContent> c : classes.values()) {
            if (c.getAnnotation(PersistenceCapable.class)!=null) {
                // if (c.getAnnotation(Entity.class)!=null) {
                if (log.isInfoEnabled()) {
                    log.info("reset() defining "+c.getName()+"("+c.getAnnotation(PersistenceCapable.class)+")");
                } // if

                try {
                    DataNucleusEnhancer enhancer = new DataNucleusEnhancer();
                    enhancer.setVerbose(true);
                    enhancer.setSystemOut(true);
                    final String classname = c.getName();
                    enhancer.setClassLoader(classRepository.getClassLoader());
                    enhancer.addClass(classname, classRepository.getBytes(classname));
                    int numClasses = enhancer.enhance();
                    // System.out.println("enhanced "+numClasses);
                    if (numClasses>0) {
                        final byte[] enhancedBytes = enhancer.getEnhancedBytes(classname);
                        classRepository.overrideClass(classname, enhancedBytes);
                        final Class<? extends Object> enhancedClass = classRepository.get(classname);
                        // System.out.println("enhanced "+enhancedClass);
                        modelClasses.add((Class<? extends MutableContent>) enhancedClass);
                    } // if
                } catch (Throwable e) {
                    log.error("reset()", e);
                } // try/catch
            } // if
        } // for
        beanFactory.setAdditionalClasses(modelClasses);
    } // reset()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        classRepository.addListener(this);
        reset();
    } // afterPropertiesSet()

} // ClassRepositoryEnhancer
