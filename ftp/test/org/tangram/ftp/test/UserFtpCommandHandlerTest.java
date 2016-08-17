/*
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
package org.tangram.ftp.test;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.ftp.SessionHelper;
import org.tangram.ftp.UserFtpCommandHandler;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the ftp command handler for renaming.
 */
public class UserFtpCommandHandlerTest extends AbstractThreadedTest {

    private static final Logger LOG = LoggerFactory.getLogger(UserFtpCommandHandlerTest.class);


    @Test
    public void testUserFtpCommandHandler() throws Exception {
        FtpTestHelper helper = new FtpTestHelper(CommandNames.USER);
        UserFtpCommandHandler userFtpCommandHandler = (UserFtpCommandHandler) helper.getHandler();

        List<String> params = new ArrayList<>();
        params.add("tangram");
        Command command = helper.getCommand(params);
        Socket socket = helper.getSocket();
        MockSession session = new MockSession(socket, helper.getCommands());
        LOG.debug("testUserFtpCommandHandler() run");
        concurrentSleep(session);
        LOG.debug("testUserFtpCommandHandler() list");
        userFtpCommandHandler.handleCommand(command, session);
        LOG.debug("testUserFtpCommandHandler() close");
        session.close();
        LOG.debug("testUserFtpCommandHandler() join");
        join();
        Object user = session.getAttribute(SessionHelper.USER);
        Assert.assertEquals(user, "tangram", "Unexpected username found in session.");
    } // testUserFtpCommandHandler()

} // UserFtpCommandHandlerTest
