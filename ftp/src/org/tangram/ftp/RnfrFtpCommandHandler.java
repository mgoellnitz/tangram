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

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.RnfrCommandHandler;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.CodeHelper;
import org.tangram.content.CodeResource;


/**
 *
 * Rename from command.
 *
 * Stores the the tangram id of the object to be renamed.
 *
 */
public class RnfrFtpCommandHandler extends RnfrCommandHandler {

    private static final Log log = LogFactory.getLog(RnfrFtpCommandHandler.class);

    private CodeResourceCache codeResourceCache;


    public RnfrFtpCommandHandler(CodeResourceCache cache) {
        codeResourceCache = cache;
    } // RnfrFtpCommandHandler()


    @Override
    public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
        for (String parameter : command.getParameters()) {
            if (log.isInfoEnabled()) {
                log.info("handleCommand() parameter "+parameter);
            } // if
        } // for
        String oldName = command.getParameter(0);
        String dir = SessionHelper.getDirectoy(session);
        if (log.isInfoEnabled()) {
            log.info("handleCommand() renaming from "+oldName+" in directory "+dir);
        } // if
        String mimetype = CodeHelper.getMimetype(dir);
        Map<String, CodeResource> typeCache = codeResourceCache.getTypeCache(mimetype);
        // directory exists?
        if (typeCache!=null) {
            CodeResource resource = typeCache.get(CodeHelper.getAnnotation(oldName));
            if (resource!=null) {
                session.setAttribute(SessionHelper.RENAME_ID, resource.getId());
            } // if
        } // if
        super.handleCommand(command, session, invocationRecord);
    } // handleCommand()

} // RnfrFtpCommandHandler
