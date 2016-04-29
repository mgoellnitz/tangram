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
package org.tangram.mutable.components.test;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.components.mutable.ToolHandler;
import org.tangram.components.test.GenericCodeResourceCacheTest;
import org.tangram.content.CodeResourceCache;
import org.tangram.content.TransientCode;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.link.TargetDescriptor;
import org.tangram.mock.MockMutableBeanFactory;
import org.tangram.mock.content.MockMutableCode;
import org.tangram.mutable.CodeHelper;
import org.tangram.protection.AuthorizationService;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the tool handler.
 */
public class ToolHandlerTest {

    private static final TargetDescriptor DUMMY_LOGIN = new TargetDescriptor(ToolHandlerTest.class, "dummy", "login");

    @Spy
    private final MockMutableBeanFactory beanFactory;

    @Spy
    private final CodeResourceCache codeCache; // NOPMD - this field is not really unused

    @Mock
    private LinkHandlerRegistry linkHandlerRegistry; // NOPMD - this field is not really unused

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private final ToolHandler toolHandler = new ToolHandler();


    public ToolHandlerTest() throws FileNotFoundException {
        codeCache = new GenericCodeResourceCacheTest().getInstance();
        beanFactory = new MockMutableBeanFactory();
        MockitoAnnotations.initMocks(this);
        toolHandler.afterPropertiesSet();
    } // ()


    @Test
    public void testClearCaches() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/clear/caches");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        try {
            toolHandler.clearCaches(request, response);
        } catch (Exception e) {
            Assert.fail("Tool handler should not issue exceptions when clearing caches.", e);
        } // try/catch
        // TODO: Mock mutable bean factory should give us some classes to deal with.
        Assert.assertEquals(beanFactory.getClearedCacheClasses().size(), 0, "Fixed number of classes to clear caches for expected.");
    } // testClearCaches()


    @Test
    public void testCodeExport() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/codes.zip");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor tool = null;
        try {
            tool = toolHandler.codeExport(request, response);
        } catch (Exception e) {
            Assert.fail("Tool handler should not issue exceptions when exporting codes.", e);
        } // try/catch
        Assert.assertEquals(tool, TargetDescriptor.DONE, "Code export indicates direct writing to writer.");
    } // testCodeExport()


    @Test
    public void testFailingCodeExport() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/codes");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor tool = null;
        try {
            tool = toolHandler.codeExport(request, response);
        } catch (Exception e) {
            Assert.fail("Tool handler should not issue exceptions when exporting codes.", e);
        } // try/catch
        Assert.assertNull(tool, "Code export with wrong URL should fail.");
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_NOT_FOUND, "Code export with wrong URL should fail.");
    } // testFailingCodeExport()


    @Test
    public void testCodeImport() {
        String id = "CodeResource:10";
        MockMutableCode code = beanFactory.getBean(MockMutableCode.class, id);
        Assert.assertNotNull(code, "We should have a bean to modify for a code update test.");
        Assert.assertEquals(code.getCodeText().length(), 564, "Initial code size expected.");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/codes");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor tool = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CRC32 crc = new CRC32();
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.setComment("Tangram Repository Codes");
            zos.setLevel(9);
            String mimeType = code.getMimeType();
            String folder = CodeHelper.getFolder(mimeType);
            String extension = CodeHelper.getExtension(mimeType);
            code.setCodeText("test");
            if (CodeHelper.getCodeMimeTypes().contains(mimeType)) {
                byte[] bytes = code.getCodeText().getBytes("UTF-8");
                ZipEntry ze = new ZipEntry(folder+"/"+code.getAnnotation().replace(';', '_')+extension);
                ze.setComment(mimeType);
                ze.setSize(bytes.length);
                ze.setTime(System.currentTimeMillis());
                crc.reset();
                crc.update(bytes);
                ze.setCrc(crc.getValue());
                zos.putNextEntry(ze);
                zos.write(bytes);
                zos.closeEntry();
            } // if

            zos.finish();
            zos.close();
            byte[] zipFile = baos.toByteArray();
            tool = toolHandler.codeImport(zipFile, request, response);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Insufficient ZIP input.");
        } // try/catch
        code = beanFactory.getBean(MockMutableCode.class, id);
        Assert.assertNotNull(code, "We should have a bean to modify for a code update test.");
        Assert.assertEquals(code.getCodeText(), "test", "Expected to find text we wrote again.");
        TargetDescriptor result = new TargetDescriptor(toolHandler, null, null);
        Assert.assertEquals(tool, result, "Code import should go back to import/export page.");
    } // testCodeImport()


    @Test
    public void testContentExport() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/export");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor tool = null;
        try {
            tool = toolHandler.contentExport(TransientCode.class.getName(), request, response);
        } catch (Exception e) {
            Assert.fail("Tool handler should not issue exceptions when exporting content.", e);
        } // try/catch
        Assert.assertEquals(tool, TargetDescriptor.DONE, "Content export indicates direct writing to writer.");
    } // testContentExport()


    @Test
    public void testEmptyContentImport() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/import");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor tool = null;
        try {
            tool = toolHandler.contentImport(null, request, response);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "You missed to select an input file.");
        } // try/catch
        Assert.assertNull(tool, "Content export indicates direct writing to writer.");
    } // testEmptyContentImport()


    @Test
    public void testSmallContentImport() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/import");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor tool = null;
        byte[] xmlfile = new byte[4];
        try {
            tool = toolHandler.contentImport(xmlfile, request, response);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Insufficient XML input.");
        } // try/catch
        Assert.assertNull(tool, "Content export indicates direct writing to writer.");
    } // testSmallContentImport()


    @Test
    public void testContentImport() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/import");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor tool = null;
        try {
            String xml = "<?xml version=\"1.0\" ?><list><Topic><id>Topic:234</id></Topic></list>";
            tool = toolHandler.contentImport(xml.getBytes("UTF-8"), request, response);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "Insufficient XML input.");
        } // try/catch
        Assert.assertEquals(tool, TargetDescriptor.DONE, "Content export indicates direct writing to writer.");
        // MockMutableBean factory does not store these values on persist() so far
        // Assert.assertNotNull(beanFactory.getBean(Topic.class, "Topic:234"), "Just imported bean should be avaiable.");
    } // testContentImport()


    @Test
    public void testImporterView() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/tools");
        request.setContextPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(true);
        TargetDescriptor tool = null;
        try {
            tool = toolHandler.importer(request, response);
        } catch (Exception e) {
            Assert.fail("Tool handler should not issue exceptions when showing the tools page.", e);
        } // try/catch
        Assert.assertNotNull(tool, "The should be a target descriptor returned.");
        Assert.assertEquals(tool.bean, toolHandler, "Tools page is viewed via tool handler instance.");
        Assert.assertNull(tool.view, "Default view expected.");
        Assert.assertNull(tool.action, "No action expected.");
    } // testImporterView()


    @Test
    public void testNoAdminCall() {
        String[] urls = {"clear/caches", "codes.zip", "export", "tools"};
        for (String url : urls) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/"+url);
            request.setContextPath("/testapp");
            HttpServletResponse response = new MockHttpServletResponse();
            Mockito.when(authorizationService.isAdminUser(request, response)).thenReturn(false);
            Mockito.when(authorizationService.getLoginTarget(request)).thenReturn(DUMMY_LOGIN);
            TargetDescriptor tool = null;
            try {
                if ("clear/caches".equals(url)) {
                    tool = toolHandler.clearCaches(request, response);
                } // if
                if ("codes.zip".equals(url)) {
                    tool = toolHandler.codeExport(request, response);
                } // if
                if ("export".equals(url)) {
                    tool = toolHandler.contentExport("", request, response);
                } // if
                if ("tools".equals(url)) {
                    tool = toolHandler.importer(request, response);
                } // if
            } catch (Exception e) {
                Assert.fail("Tool handler should not issue exceptions when redirecting to login.", e);
            } // try/catch
            Assert.assertEquals(tool, DUMMY_LOGIN, "Non logged in calls should issue login redirect.");
        } // for
    } // testNoAdminCall()

} // ToolHandlerTest
