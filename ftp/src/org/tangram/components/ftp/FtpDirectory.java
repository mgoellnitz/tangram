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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.components.ftp;

import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.tangram.components.spring.CodeResourceCache;
import org.tangram.content.BeanFactory;
import org.tangram.ftp.TangramFtpServer;
import org.tangram.mutable.MutableBeanFactory;


/**
 *
 * Connector component to let the code aspects of a tangram repository be accessible as an ftp remote server.
 *
 */
@Named
public class FtpDirectory implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(FtpDirectory.class);

    private TangramFtpServer ftpServerStub;

    @Inject
    private BeanFactory beanFactory;

    @Inject
    private CodeResourceCache codeResourceCache;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (log.isInfoEnabled()) {
            log.info("() initializing with code cache "+codeResourceCache);
        } // if
        if (beanFactory instanceof MutableBeanFactory) {
            ftpServerStub = new TangramFtpServer((MutableBeanFactory) beanFactory, codeResourceCache);
            if (log.isInfoEnabled()) {
                log.info("() starting");
            } // if
            ftpServerStub.start();
        } else {
            log.error("afterPropertiesSet() no factory for mutable beans - not starting ftp service");
        } // if
    } // afterPropertiesSet()

} // FtpDirectory
