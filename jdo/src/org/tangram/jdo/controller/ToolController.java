package org.tangram.jdo.controller;

//import java.beans.PropertyDescriptor;
//import java.beans.XMLEncoder;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.Reader;
import java.lang.reflect.Modifier;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;
// import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// import javax.jdo.JDOHelper;
// import javax.jdo.PersistenceManager;
// import javax.jdo.PersistenceManagerFactory;
// import javax.jdo.annotations.NotPersistent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
// import org.springframework.beans.BeanWrapper;
// import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestMethod;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.controller.RenderingController;
import org.tangram.jdo.JdoBeanFactory;
import org.tangram.monitor.StatisticsController;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;

/*
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
*/

@Controller
public class ToolController extends RenderingController {

    private static final Log log = LogFactory.getLog(ToolController.class);

    @Autowired
    private StatisticsController statisticsController;


    @Override
    public void setLinkFactory(LinkFactory linkFactory) {
        // Don't register with link generation - this one doesn't generate links
    } // setLinkFactory()

  
    @RequestMapping(value = "/clear/caches")
    public ModelAndView clearCaches(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (request.getParameter(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new IOException("User may not clear cache");
            } // if

            JdoBeanFactory jdoBeanFactory = (JdoBeanFactory)beanFactory;

            if (log.isInfoEnabled()) {
                log.info("clearCaches() clearing class specific caches");
            } // if
            for (Class<? extends Content> c : jdoBeanFactory.getClasses()) {
                if (c.isInterface()||(c.getModifiers()&Modifier.ABSTRACT)>0) {
                    if (log.isInfoEnabled()) {
                        log.info("clearCaches() "+c.getSimpleName()+" may not have instances");
                    } // if
                } else {
                    jdoBeanFactory.clearCacheFor(c);
                } // if
            } // for

            return modelAndViewFactory.createModelAndView(statisticsController, request, response);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // clearCaches()


    private String getFilename(CodeResource code) {
        return code.getAnnotation().replace(';', '_');
    } // getFilename()


    @RequestMapping(value = "/codes")
    public ModelAndView codes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if ( !request.getRequestURI().endsWith(".zip")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new IOException("User may not execute action");
            } // if

            long now = System.currentTimeMillis();

            response.setContentType("application/x-zip-compressed");

            CRC32 crc = new CRC32();

            ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
            zos.setComment("Tangram Google AppEngine Codes");
            zos.setLevel(9);
            List<CodeResource> codes = beanFactory.listBeans(CodeResource.class);
            for (CodeResource code : codes) {
                if (StringUtils.hasText(code.getAnnotation())) {
                    String mimeType = code.getMimeType();
                    if ("application/x-groovy".equals(mimeType)) {
                        mimeType = "text/groovy";
                    } // if
                    if ("text/html".equals(mimeType)) {
                        mimeType = "text/vtl";
                    } // if
                    if ("text/xml".equals(mimeType)) {
                        mimeType = "text/vtl";
                    } // if
                    if ("text/javascript".equals(mimeType)) {
                        mimeType = "text/js";
                    } // if
                    if (mimeType.startsWith("text/")) {
                        String subType = mimeType.substring(5);
                        byte[] bytes = code.getCodeText().getBytes("UTF-8");
                        ZipEntry ze = new ZipEntry(subType+"/"+getFilename(code)+"."+subType);
                        ze.setTime(now);
                        crc.reset();
                        crc.update(bytes);
                        ze.setCrc(crc.getValue());
                        zos.putNextEntry(ze);
                        zos.write(bytes);
                        zos.closeEntry();
                    } // if
                } // if
            } // for
            zos.finish();
            zos.close();

            return null;
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // codes()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        return null;
    } // createLink()

} // ToolController
