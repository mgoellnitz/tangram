/**
 *
 * Copyright 2014 Martin Goellnitz
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
package org.tangram.view.test;

import java.io.UnsupportedEncodingException;
import org.junit.Assert;
import org.junit.Test;
import org.tangram.view.Utils;


public class UtilsTest {

    @Test
    public void testUtils() {
        String urlPart = null;
        try {
            urlPart = Utils.urlize("Hello dear user, we'd like to test some german umlauts like öäüßÖÄÜ - done.");
        } catch (UnsupportedEncodingException uee) {
            Assert.fail("Unpectedtedly even the encoding of the test's text didn't work.");
        } // try/catchg
        Assert.assertNotNull("Encoding not supported", urlPart);
        Assert.assertEquals("Special characters and spaces should be transcoded", "hello-dear-user--we-d-like-to-test-some-german-umlauts-like-oeaeuessoeaeue-done.", urlPart);
    } // testUtils()

} // UtilsTest
