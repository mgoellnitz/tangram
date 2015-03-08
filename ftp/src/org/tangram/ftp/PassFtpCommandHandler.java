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

import java.util.Properties;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.command.ReplyCodes;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.PassCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.CodeResource;
import org.tangram.util.SystemUtils;


/**
 *
 * Password entering ftp command.
 *
 * Takes the passed over password and checks if the user is to be logged in.
 * User database is taken from the tangram code resource cache.
 *
 */
public class PassFtpCommandHandler extends PassCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PassFtpCommandHandler.class);

    private final CodeResourceCache codeResourceCache;


    public PassFtpCommandHandler(CodeResourceCache codeResourceCache) {
        this.codeResourceCache = codeResourceCache;
    }


    @Override
    public void handleCommand(Command command, Session session, InvocationRecord invocationRecord) {
        String user = (String) session.getAttribute(SessionHelper.USER);
        setReplyCode(ReplyCodes.PASS_LOG_IN_FAILED);
        if (user!=null) {
            String pass = command.getParameter(0);
            LOG.info("handleCommand() logging in with password {}", pass);
            if (pass!=null) {
                CodeResource code = codeResourceCache.getTypeCache("text/plain").get("ftp-users.properties");
                Properties p = new Properties();
                try {
                    p.load(code.getStream());
                    pass = SystemUtils.getSha256Hash(pass);
                } catch (Exception e) {
                    LOG.error("handleCommand() error while reading user database", e);
                } // try/catch
                if (pass.equals(p.getProperty(user))) {
                    setReplyCode(ReplyCodes.PASS_OK);
                } // if
            } else {
                LOG.error("handleCommand() no password issued");
            } // if
        } else {
            LOG.error("handleCommand() no user issued");
        } // if
        sendReply(session);
    } // handleCommand()

} // PassFtpCommandHandler
