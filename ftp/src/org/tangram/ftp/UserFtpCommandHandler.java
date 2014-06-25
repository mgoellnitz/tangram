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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.ftp;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.UserCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * Welcome user ftp command.
 *
 * Stores the user name of the user in the session attributes store.
 *
 */
public class UserFtpCommandHandler extends UserCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UserFtpCommandHandler.class);


    @Override
    public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
        String user = command.getParameter(0);
        if (LOG.isInfoEnabled()) {
            LOG.info("handleCommand() welcome to user "+user);
        } // if
        session.setAttribute(SessionHelper.USER, user);
        super.handleCommand(command, session, invocationRecord);
    } // handleCommand()

} // UserFtpCommandHandler
