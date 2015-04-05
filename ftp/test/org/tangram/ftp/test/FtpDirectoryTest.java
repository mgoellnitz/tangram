/**
 *
 * Copyright 2015 Martin Goellnitz
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

import dinistiq.Dinistiq;
import java.util.HashSet;
import java.util.Set;
import org.tangram.components.ftp.FtpDirectory;
import org.testng.Assert;
import org.testng.annotations.Test;


public class FtpDirectoryTest {

    @Test
    public void testFtpComponent() throws Exception {
        Set<String> packages = new HashSet<>();
        packages.add("org.tangram.components");
        Dinistiq dinistiq = new Dinistiq(packages);
        FtpDirectory ftp = dinistiq.findBean(FtpDirectory.class);
        Assert.assertNotNull(ftp, "Could not find ftp directory instance");
    } // testFtpComponent()

} // FtpDirectoryTest
