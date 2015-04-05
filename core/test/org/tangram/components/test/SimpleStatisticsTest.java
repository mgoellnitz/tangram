/**
 *
 * Copyright 2014-2015 Martin Goellnitz
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
package org.tangram.components.test;

import java.util.Map;
import org.tangram.components.SimpleStatistics;
import org.testng.Assert;
import org.testng.annotations.Test;


public class SimpleStatisticsTest {

    @Test
    public void testSimpleStatistics() {
        SimpleStatistics s = new SimpleStatistics();
        s.increase("test");
        s.increase("test");
        s.avg("avg", 2);
        s.avg("avg", 4);
        s.avg("avg", 6);
        Map<String, Long> counter = s.getCounter();
        Assert.assertEquals((long) counter.get("test"), 2, "we tried to count two events");
        Assert.assertEquals((long) counter.get("avg"), 4, "average value should be 4");
    } // testSimpleStatistics()

} // SimpleStatisticsTest
