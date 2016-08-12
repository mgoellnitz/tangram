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
import org.tangram.ftp.StorFtpCommandHandler;
import org.testng.annotations.Test;


/**
 * Test aspects of the ftp command handler for renaming.
 */
public class StorFtpCommandHandlerTest extends AbstractThreadedTest {

    private static final Logger LOG = LoggerFactory.getLogger(StorFtpCommandHandlerTest.class);


    @Test
    public void testStorFtpCommandHandler() throws Exception {
        FtpTestHelper helper = new FtpTestHelper(CommandNames.STOR);
        StorFtpCommandHandler storFtpCommandHandler = (StorFtpCommandHandler) helper.getHandler();
        List<String> params = new ArrayList<>();
        params.add("org.tangram.link.GroovyLinkHandler.groovy");
        params.add("org.tangram.link.GroovyLinkHandler.groovy");
        Command command = helper.getCommand(params);
        Socket socket = helper.getSocket();
        MockSession session = new MockSession(socket, helper.getCommands());
        LOG.debug("testRntoFtpCommandHandler() run");
        concurrentSleep(session);
        LOG.debug("testRntoFtpCommandHandler() list");
        session.setAttribute(SessionHelper.CURRENT_DIR, "/groovy");
        storFtpCommandHandler.handleCommand(command, session);
        LOG.debug("testRntoFtpCommandHandler() close");
        session.close();
        LOG.debug("testRntoFtpCommandHandler() join");
        join();
    } // testStorFtpCommandHandler()

} // StorFtpCommandHandlerTest
