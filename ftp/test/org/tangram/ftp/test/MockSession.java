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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Map;
import org.mockftpserver.core.MockFtpServerException;
import org.mockftpserver.core.command.AbstractTrackingCommandHandler;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.session.DefaultSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mock ftp server's session interface.
 */
public class MockSession extends DefaultSession {

    private static final Logger LOG = LoggerFactory.getLogger(MockSession.class);

    private ByteArrayInputStream dataInputStream;

    private ByteArrayOutputStream dataOutputStream;


    /**
     * Create a new initialized instance
     *
     * @param controlSocket - the control connection socket
     * @param commandHandlers - the Map of command name -> CommandHandler. It is assumed that the
     * command names are all normalized to upper case. See {@link Command#normalizeName(String)}.
     */
    public MockSession(Socket controlSocket, Map<String, AbstractTrackingCommandHandler> commandHandlers) {
        super(controlSocket, commandHandlers);
    } // ()


    /**
     * @see org.mockftpserver.core.session.Session#openDataConnection()
     */
    public void openDataConnection() {
        try {
            LOG.debug("openDataConnection()");
            dataOutputStream = new ByteArrayOutputStream();
            dataInputStream = new ByteArrayInputStream("".getBytes("UTF-8"));
        } catch (IOException e) {
            throw new MockFtpServerException(e);
        }
    } // openDataConnection()


    /**
     * @see org.mockftpserver.core.session.Session#closeDataConnection()
     */
    public void closeDataConnection() {
        try {
            LOG.debug("closeDataConnection() Flushing and closing client data socket");
            dataOutputStream.flush();
            dataOutputStream.close();
            dataInputStream.close();
        } catch (IOException e) {
            LOG.error("closeDataConnection() Error closing client data socket", e);
        }
    } // closeDataConnection()


    public String getContent() {
        try {
            return dataOutputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return e.getMessage();
        } /// try/catch
    } // getBytest()


    /**
     * @see org.mockftpserver.core.session.Session#sendData(byte[], int)
     */
    public void sendData(byte[] data, int numBytes) {
        LOG.debug("sendData()");
        dataOutputStream.write(data, 0, numBytes);
    } // sendData()


    /**
     * @see org.mockftpserver.core.session.Session#readData()
     */
    public byte[] readData() {
        LOG.debug("readData()");
        return readData(Integer.MAX_VALUE);
    } // readData()


    /**
     * @see org.mockftpserver.core.session.Session#readData()
     */
    public byte[] readData(int numBytes) {
        LOG.debug("readData() bytes={}", numBytes);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int numBytesRead = 0;
        while (numBytesRead<numBytes) {
            int b = dataInputStream.read();
            if (b==-1) {
                break;
            }
            bytes.write(b);
            numBytesRead++;
        }
        return bytes.toByteArray();
    } // readData()

} // MockSession
