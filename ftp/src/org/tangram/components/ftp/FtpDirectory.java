/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
package org.tangram.components.ftp;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.BeanFactory;
import org.tangram.ftp.TangramFtpServer;
import org.tangram.mutable.MutableBeanFactory;


/**
 *
 * Connector component to let the code aspects of a tangram repository be accessible as an ftp remote server.
 *
 */
@Named
@Singleton
public class FtpDirectory {

    private static final Logger LOG = LoggerFactory.getLogger(FtpDirectory.class);

    private TangramFtpServer ftpServerStub;

    @Inject
    private BeanFactory beanFactory;

    @Inject
    private CodeResourceCache codeResourceCache;

    private int ftpPort = 20021;


    /**
     * Set the port to be used for the ftp service.
     * It default to 20021 - not the ftp default service port 21.
     *
     * @param ftpPort port number to be used.
     */
    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.info("afterPropertiesSet() initializing with code cache {}", codeResourceCache);
        if (beanFactory instanceof MutableBeanFactory) {
            ftpServerStub = new TangramFtpServer((MutableBeanFactory) beanFactory, codeResourceCache);
            LOG.info("afterPropertiesSet() starting on port {}", ftpPort);
            ftpServerStub.setServerControlPort(ftpPort);
            ftpServerStub.start();
        } else {
            LOG.error("afterPropertiesSet() no factory for mutable beans - not starting ftp service");
        } // if
    } // afterPropertiesSet()

} // FtpDirectory
