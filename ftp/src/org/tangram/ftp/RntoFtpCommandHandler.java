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
package org.tangram.ftp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.RntoCommandHandler;
import org.tangram.components.CodeResourceCache;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableCode;


/**
 *
 * Rename to ftp command.
 *
 * Take the stored ID from the session and renamed the references object to the given name.
 *
 */
public class RntoFtpCommandHandler extends RntoCommandHandler {

    private static final Log log = LogFactory.getLog(RntoFtpCommandHandler.class);

    private MutableBeanFactory beanFactory;

    private CodeResourceCache codeResourceCache;


    public RntoFtpCommandHandler(MutableBeanFactory beanFactory, CodeResourceCache codeResourceCache) {
        this.beanFactory = beanFactory;
        this.codeResourceCache = codeResourceCache;
    }


    @Override
    public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
        String newName = command.getParameter(0);
        String id = (String) session.getAttribute(SessionHelper.RENAME_ID);
        if (log.isInfoEnabled()) {
            log.info("handleCommand() renaming "+id+" to "+newName);
        } // if
        if (id!=null) {
            MutableCode code = beanFactory.getBeanForUpdate(beanFactory.getCodeClass(), id);
            if (code!=null) {
                code.setAnnotation(SessionHelper.getAnnotation(newName));
                code.persist();
            } // if
        } // if
        super.handleCommand(command, session, invocationRecord);
    } // handleCommand()

} // RntoFtpCommandHandler
