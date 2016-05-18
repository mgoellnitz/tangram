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

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.ftp.ListFtpCommandHandler;
import org.tangram.ftp.SessionHelper;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the ftp command handler for listing folders.
 */
public class ListFtpCommandHandlerTest extends AbstractThreadedTest {

    private static final Logger LOG = LoggerFactory.getLogger(ListFtpCommandHandlerTest.class);


    @Test
    public void testListFtpCommandHandler() throws Exception {
        FtpTestHelper helper = new FtpTestHelper(CommandNames.LIST);
        ListFtpCommandHandler listFtpCommandHandler = (ListFtpCommandHandler) helper.getHandler();
        List<String> params = new ArrayList<>();
        Command command = helper.getCommand(params);
        Socket socket = helper.getSocket();
        MockSession session = new MockSession(socket, helper.getCommands());
        LOG.debug("testListFtpCommandHandler() run");
        concurrentSleep(session);
        LOG.debug("testListFtpCommandHandler() list");
        listFtpCommandHandler.handleCommand(command, session);
        session.setAttribute(SessionHelper.CURRENT_DIR, "/groovy");
        listFtpCommandHandler.handleCommand(command, session);
        LOG.debug("testListFtpCommandHandler() close");
        session.close();
        LOG.debug("testListFtpCommandHandler() join");
        join();
        BufferedReader reader = new BufferedReader(new StringReader(session.getContent()));
        String line = reader.readLine();
        while (line!=null) {
            Assert.assertTrue(line.startsWith("-rw-r--r--   1 tangram tangram"), "Error in line format discovered.");
            Assert.assertTrue(line.endsWith(".groovy"), "Error in line format discovered.");
            line = reader.readLine();
        } // while
    } // testListFtpCommandHandler()

} // ListFtpCommandHandlerTest
