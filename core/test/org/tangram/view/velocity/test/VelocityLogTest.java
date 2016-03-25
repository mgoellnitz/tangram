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
package org.tangram.view.velocity.test;

import org.tangram.view.velocity.VelocityLog;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Just trigger the log somehow.
 */
public class VelocityLogTest {

    @Test
    public void testVelocityLog() {
        VelocityLog log = new VelocityLog();
        Assert.assertTrue(log.isLevelEnabled(VelocityLog.DEBUG_ID), "Level debug should be enabled during test.");
        Assert.assertTrue(log.isLevelEnabled(VelocityLog.ERROR_ID), "Level error should be enabled during test.");
        Assert.assertTrue(log.isLevelEnabled(VelocityLog.INFO_ID), "Level info should be enabled during test.");
        Assert.assertFalse(log.isLevelEnabled(VelocityLog.TRACE_ID), "Level trace should be disabled during test.");
        Assert.assertTrue(log.isLevelEnabled(VelocityLog.WARN_ID), "Level warn should be enabled during test.");
    } // testVelocityLog()

} // VelocityLogTest
