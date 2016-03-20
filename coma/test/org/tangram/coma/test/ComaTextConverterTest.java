/*
 *
 * Copyright 2015-2016 Martin Goellnitz
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
package org.tangram.coma.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.coma.ComaTextConverter;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Do some very basic test if the assembly of a string from the various sources works.
 *
 * Deliberately copied from CoConAT project which will some day become a dependency.
 */
@Test
public class ComaTextConverterTest {

    private static final Logger LOG = LoggerFactory.getLogger(ComaTextConverterTest .class);


    @Test
    public void testConverter() {
        StringBuilder text = new StringBuilder(80);
        text.append("Enjoy the taste of a duck cutlet combined with caramelized onions. A dream!");
        LOG.debug("testConverter() text={}", text.toString());
        StringBuilder data = new StringBuilder(140);
        data.append("(0003diva0005xmlns002Bhttp://www.coremedia.com/2003/richtext-1.0]a000Bxmlns:xlink001Dhttp://www.w3.org/1999/xlink](0001p-004B)0001p)0003div");
        LOG.debug("testConverter() data={}", data.toString());
        String result = ComaTextConverter.convert(text, data);
        LOG.debug("testConverter() result={}", result);
        Assert.assertNotNull(result, "Result should not be null");
        String reference = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><p>Enjoy the taste of a duck cutlet combined with caramelized onions. A dream!</p></div>";
        Assert.assertEquals(result, reference, "Wrong conversion result");
    } // testConverter()

} // ComaTextConverterTest
