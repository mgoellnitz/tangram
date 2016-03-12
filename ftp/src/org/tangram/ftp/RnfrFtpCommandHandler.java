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
package org.tangram.ftp;

import java.util.Map;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.RnfrCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.mutable.CodeHelper;


/**
 *
 * Rename from command.
 *
 * Stores the the tangram id of the object to be renamed.
 *
 */
public class RnfrFtpCommandHandler extends RnfrCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RnfrFtpCommandHandler.class);

    private final CodeResourceCache codeResourceCache;


    public RnfrFtpCommandHandler(CodeResourceCache cache) {
        codeResourceCache = cache;
    } // RnfrFtpCommandHandler()


    @Override
    public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
        for (String parameter : command.getParameters()) {
            LOG.info("handleCommand() parameter {}", parameter);
        } // for
        String oldName = command.getParameter(0);
        String dir = SessionHelper.getDirectoy(session);
        LOG.info("handleCommand() renaming from {} in directory {}", oldName, dir);
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
