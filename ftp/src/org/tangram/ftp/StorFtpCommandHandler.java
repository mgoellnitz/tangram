/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.StorCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.CodeHelper;
import org.tangram.content.CodeResource;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableCode;


/**
 *
 */
public class StorFtpCommandHandler extends StorCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(StorFtpCommandHandler.class);

    private final MutableBeanFactory beanFactory;

    private final CodeResourceCache codeResourceCache;


    public StorFtpCommandHandler(MutableBeanFactory beanFactory, CodeResourceCache codeResourceCache) {
        this.beanFactory = beanFactory;
        this.codeResourceCache = codeResourceCache;
    } // StorFtpCommandHandler()


    @Override
    protected void afterProcessData(Command command, Session session, InvocationRecord invocationRecord) throws Exception {
        super.afterProcessData(command, session, invocationRecord);
        byte[] data = (byte[]) invocationRecord.getObject(FILE_CONTENTS_KEY);
        String dir = SessionHelper.getCwd(session);
        String filename = invocationRecord.getString(PATHNAME_KEY);

        if ((!filename.startsWith("//netbeans-timestampdiff"))&&(dir.length()>0)) {
            dir = SessionHelper.getDirectoy(session);
            String mimetype = CodeHelper.getMimetype(dir);
            String annotation = CodeHelper.getAnnotation(filename);
            CodeResource lookup = codeResourceCache.get(mimetype, annotation);
            // This hopefully is one really just one class effectively
            Class<? extends MutableCode> codeClass = beanFactory.getImplementingClasses(MutableCode.class).get(0);
            MutableCode code = (lookup==null) ? beanFactory.createBean(codeClass) : beanFactory.getBean(codeClass, lookup.getId());
            beanFactory.beginTransaction();
            code.setAnnotation(annotation);
            code.setCode(new String(data, "UTF-8").toCharArray());
            code.setMimeType(mimetype);
            beanFactory.persist(code);
        } // if

        LOG.info("afterProcessData() {} / {} ({}b)", dir, filename, data.length);
    } // afterProcessData()

} // StorFtpCommandHandler
