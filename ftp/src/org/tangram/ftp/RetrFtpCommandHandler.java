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
import org.mockftpserver.stub.command.RetrCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.CodeHelper;
import org.tangram.content.CodeResource;


/**
 *
 * Change workgin dir ftp command.
 *
 * Stores the new working dir name in the session attributes store.
 *
 */
public class RetrFtpCommandHandler extends RetrCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RetrFtpCommandHandler.class);

    private CodeResourceCache codeResourceCache;


    public RetrFtpCommandHandler(CodeResourceCache codeResourceCache) {
        this.codeResourceCache = codeResourceCache;
    } // RetrFtpCommandHandler()


    @Override
    protected void processData(Command command, Session session, InvocationRecord invocationRecord) {
        String filename = command.getParameter(0);
        byte[] data = new byte[0];
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("processData() retrieving "+filename);
            } // if
            String type = CodeHelper.getMimetype(SessionHelper.getDirectoy(session));
            String annotation = filename.substring(0, filename.lastIndexOf('.'));
            Map<String, CodeResource> cache = codeResourceCache.getTypeCache(type);
            if (LOG.isInfoEnabled()) {
                LOG.info("processData() 'directory' for "+type+" is "+cache);
            } // if
            final String key = "text/plain".equals(type) ? filename : annotation;
            CodeResource code = cache.get(key);
            if (LOG.isInfoEnabled()) {
                LOG.info("processData() code for "+key+" is "+code);
            } // if
            data = code.getCodeText().getBytes("UTF-8");
        } catch (Exception e) {
            LOG.error("processData()", e);
        } // try/catch
        if (LOG.isInfoEnabled()) {
            LOG.info("processData() sending "+data.length+" bytes");
        } // if
        session.sendData(data, data.length);
    } // processData()

} // RetrCommandHandler
