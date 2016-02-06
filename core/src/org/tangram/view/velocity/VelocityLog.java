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
package org.tangram.view.velocity;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Log adaptor for the velocity engine to use slf4j like the rest of this framework.
 */
public class VelocityLog implements LogChute {

    private static final Logger LOGGER = LoggerFactory.getLogger(VelocityLog.class);


    @Override
    public void init(RuntimeServices rs) throws Exception {
        LOGGER.debug("Log Chute initialized");
    } // init()


    @Override
    public void log(int level, String msg) {
        switch (level) {
            case LogChute.DEBUG_ID:
                LOGGER.debug(msg);
                break;
            case LogChute.INFO_ID:
                LOGGER.info(msg);
                break;
            case LogChute.WARN_ID:
                LOGGER.warn(msg);
                break;
            case LogChute.ERROR_ID:
                LOGGER.error(msg);
                break;
            default:
                break;
        } // switch
    } // log()


    @Override
    public void log(int level, String msg, Throwable thrwbl) {
        switch (level) {
            case LogChute.DEBUG_ID:
                LOGGER.debug(msg, thrwbl);
                break;
            case LogChute.INFO_ID:
                LOGGER.info(msg, thrwbl);
                break;
            case LogChute.WARN_ID:
                LOGGER.warn(msg, thrwbl);
                break;
            case LogChute.ERROR_ID:
                LOGGER.error(msg, thrwbl);
                break;
            default:
                break;
        } // switch
    } // log()


    @Override
    public boolean isLevelEnabled(int level) {
        boolean result = ((level==LogChute.DEBUG_ID)&&LOGGER.isDebugEnabled());
        result = result||((level==LogChute.INFO_ID)&&LOGGER.isInfoEnabled());
        result = result||((level==LogChute.WARN_ID)&&LOGGER.isWarnEnabled());
        result = result||((level==LogChute.ERROR_ID)&&LOGGER.isErrorEnabled());
        return result;
    } // isLevelEnabled()

} // VelocityLog
