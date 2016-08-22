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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tangram.coma.ComaBeanFactory;
import org.tangram.coma.ComaBeanPopulator;
import org.tangram.coma.ComaBlob;
import org.tangram.coma.ComaContent;
import org.tangram.components.coma.test.ComaTestCodeBeanPopulator;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.mock.content.Topic;
import org.tangram.util.SystemUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Do some very basic test accessing a prepared read only database from CoreMedia 5.0 with hsqldb.
 */
public class ComaBeanFactoryTest {

    private static final String DB_DRIVER = "org.hsqldb.jdbcDriver";

    private static final String DB_URL = "jdbc:hsqldb:test/unittest;readonly=true";

    private static final String DB_USER = "sa";

    private static final String DB_PASSWORD = "";


    /**
     * Create coma bean factory with user sa for hsqldb and the given url.
     *
     * @return coma bean factory instance
     */
    private ComaBeanFactory createFactory(String dbUrl) {
        ComaBeanFactory factory = new ComaBeanFactory();
        Map<String, String> parents = new HashMap<>();
        parents.put("Topic", "Linkable");
        factory.setParents(parents);
        factory.setDbUrl(dbUrl);
        factory.setDbDriver(DB_DRIVER);
        factory.setDbUser(DB_USER);
        factory.setDbPassword(DB_PASSWORD);
        Set<String> codeTypeNames = new HashSet<>(1);
        codeTypeNames.add("Topic");
        codeTypeNames.add("RootTopic");
        factory.setCodeTypeNames(codeTypeNames);
        Set<ComaBeanPopulator> populators = new HashSet<>(1);
        populators.add(new ComaTestCodeBeanPopulator());
        try {
            Field populatorField = factory.getClass().getDeclaredField("populators");
            populatorField.setAccessible(true);
            populatorField.set(factory, populators);
            populatorField.setAccessible(false);
        } catch (NoSuchFieldException|IllegalAccessException e) {
            Assert.fail("could not create coma bean factory for some unexpected reason", e);
        } // try/catch
        factory.afterPropertiesSet();
        return factory;
    } // createFactory()


    /**
     * Test basic repository functions.
     */
    @Test
    public void testRepository() {
        ComaBeanFactory factory = createFactory(DB_URL);
        ComaContent home = factory.getBean(ComaContent.class, factory.getChildId("CoConAT/Home"));
        Assert.assertNotNull(home, "root topic 'Home' not found");
        Assert.assertFalse(home.isEmpty(), "root topic must not have an empty property set");
        Assert.assertEquals(home.entrySet().size(), 16, "Unexpected number of properties for root topic");
        Assert.assertEquals(home.get("title"), "CoConAT", "Unexpected title found");
        Assert.assertEquals(home.get("teaser"), "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><p>CoreMedia Content Access Tool. A far too simple library to access basic CoreMedia CMS content objects directly from the database using different languages for integration purposes.</p></div>", "Unexpected teaser found");
        Assert.assertTrue(home.containsKey("keywords"), "root topic should contain property keywords");
        Assert.assertTrue(home.containsValue("CoConAT"), "root topic should property value CoConAT in some property");
        Object l = home.get("logo");
        Assert.assertNotNull(l, "no logo found in root topic");
        List<ComaContent> logos = SystemUtils.convert(l);
        Assert.assertEquals(logos.size(), 1, "Expected to find exactly one logo");
        ComaContent logo = logos.get(0);
        Assert.assertEquals(logo.keySet().size(), logo.values().size(), "Size of keys must match size of values for logo");
        Assert.assertEquals(logo.size(), 17, "Unexpected number of properties for logo");
        Assert.assertEquals(logo.get("width"), "200", "Unexpected width in logo");
        Assert.assertEquals(logo.get("height"), "94", "Unexpected height in logo");
        Assert.assertEquals(logo.getId(), "10", "Unexpected id for logo");
        Assert.assertEquals(""+logo, "10 :ImageData", "Unexpected string representation for logo");
        Object b = logo.get("data");
        Assert.assertNotNull(b, "no blob found in logo object");
        ComaBlob blob = (ComaBlob) b;
        Assert.assertEquals(blob.getLen(), 10657, "Unexpected number of bytes in blob");
        Assert.assertEquals(blob.getMimeType(), "image/png", "Unexpected mime type in blob");
        Assert.assertEquals(blob.getContentId(), "10", "Unexpected content id reference in blob");
        Assert.assertEquals(blob.getPropertyName(), "data", "Unexpected property name reference in blob");
        Object s = home.get("subTopics");
        Assert.assertNotNull(s, "no subtopics found in root topic");
        home.remove("title");
        Assert.assertNull(home.get("title"), "Unexpected title found");
        home.put("title", "new title");
        Assert.assertEquals(home.get("title"), "new title", "Unexpected title found");
        home.clear();
        home.putAll(Collections.emptyMap());
        Assert.assertNull(home.get("title"), "Unexpected title found");
        List<Topic> contents = factory.listBeans(Topic.class);
        Assert.assertNotNull(contents, "There should be some result when listing all beans.");
        Assert.assertEquals(contents.size(), 2, "We have a certain number of prepared beans available.");
    } // testRepository()


    /**
     * Test code related methods.
     */
    @Test
    public void testCodes() {
        ComaBeanFactory factory = createFactory(DB_URL);
        List<CodeResource> contents = factory.listBeans(CodeResource.class);
        Assert.assertNotNull(contents, "There should be some result when listing code resources.");
        Assert.assertEquals(contents.size(), 2, "We have a fixed number of code resources available.");
        CodeResource code = contents.get(1);
        Assert.assertEquals(code.getAnnotation(), "org.tangram.example.Topic", "Unexpected annotation for code.");
        Assert.assertEquals(code.getMimeType(), "text/html", "Unexpected mime type for code.");
        Assert.assertEquals(code.getSize(), 58, "Unexpected size for code.");
    } // testCodes()


    @Test
    public void testInitFailure() {
        ComaBeanFactory factory = createFactory("jdbc:xsqldb:test/unittest;readonly=true");
        Assert.assertNull(factory.getChildId("CoConAT/Home"), "We should not be able to find a folder.");
    } // testInitFailure()


    /**
     * Test of additional api elements.
     */
    @Test
    public void testImplementation() {
        ComaBeanFactory factory = createFactory(DB_URL);
        String homeFolderId = factory.getChildId("CoConAT");
        Assert.assertEquals(homeFolderId, "9", "Unexpected id for home folder");
        Set<Content> topicSet = factory.getChildrenWithType(homeFolderId, "Topic");
        Assert.assertEquals(topicSet.size(), 2, "Unexpected number of topics");
        Set<String> topicIds = factory.listIds("Topic", "AND lastname_ = 'coconat.php'", null, true);
        Assert.assertEquals(topicIds.size(), 1, "Unexpected number of topics with a certain name");
        Set<Content> children = factory.getChildren("9", "coco.*");
        Assert.assertEquals(children.size(), 2, "Unexpected number of topics");
        Set<String> ids = factory.listIds("Topic", null, "id_", false);
        Assert.assertEquals(ids.size(), 2, "Unexpected total number of topics");
        Assert.assertEquals(ids.iterator().next(), "6", "Unexpected id of first topic in list");
        Map<String, Object> ap = new HashMap<>();
        ap.put("additionalProperty", "Value");
        factory.setAdditionalProperties(ap);
        Assert.assertEquals(factory.getAdditionalProperties().get("additionalProperty"), "Value", "Unexpected id of first topic in list");
        ids = factory.getChildrenIds("1");
        Assert.assertEquals(ids.size(), 3, "Unexpected number of children IDs for root folder.");
        Assert.assertEquals(factory.getParents().size(), 1, "Unexpected initial parents collection found.");
        Map<String, String> parents = new HashMap<>();
        factory.setParents(parents);
        Assert.assertEquals(factory.getParents(), parents, "Unexpected parents collection found.");
        Set<String> referrerIds = factory.getReferrerIds("6", "Topic", "subTopics");
        Assert.assertEquals(referrerIds.size(), 1, "Unexpected referrer set found.");
    } // testImplementation()

} // ComaBeanFactoryTest
