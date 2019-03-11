/**
 *
 * Copyright 2011-2019 Martin Goellnitz, Markus Goellnitz
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
package org.tangram.view;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.content.Markdown;
import org.tangram.util.SystemUtils;


/**
 * Used to convert human readable values from forms to objects and vice versa.
 *
 * We had the idea of replacing this with spring conversion service but didn't
 * succeed. Now this turned out to be more portable since we don't use spring in
 * all scenarios anymore.
 */
public abstract class AbstractPropertyConverter implements PropertyConverter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPropertyConverter.class);

    private DateFormat dateFormat = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);

    @Inject
    private BeanFactory<?> beanFactory;

    @Inject
    private ViewUtilities viewUtilties;


    public void setDateFormat(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
    } // setDateFormat()


    /**
     * @see PropertyConverter#getEditString(java.lang.Object)
     */
    @Override
    public String getEditString(Object o) {
        try {
            if (o==null) {
                return "";
            } // if
            if (o instanceof List) {
                StringBuilder result = new StringBuilder("");
                try {
                    List<? extends Object> list = (List<? extends Object>) o;
                    for (Object i : list) {
                        if (i instanceof Content) {
                            result.append(((Content) i).getId());
                            result.append(", ");
                        } else {
                            result.append(i);
                            result.append(',');
                        } // if
                    } // for
                } catch (Exception ex) {
                    LOG.error("getEditString() Loss of references on editing a list. Be warned and check your content.", ex);
                } // try/catch
                return result.toString();
            } else {
                if (o instanceof Boolean) {
                    return o.toString();
                } else {
                    if (o instanceof Content) {
                        return ((Content) o).getId();
                    } else {
                        if (o instanceof Date) {
                            return dateFormat.format(o);
                        } else {
                            return o.toString();
                        } // if
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("getEditString()", e);
            return "error while converting "+o;
        } // try/catch
    } // getEditString()


    /**
     * Obtain a list of content objects from their respective renderings with
     * the view 'description'. This view is shown in the editor and thus it
     * seems a good idea to let the user enter it. We need a viewing context in
     * the form of a request to do this.
     *
     * @param <T> class constraint of the objects to return
     * @param c class of the objects to return
     * @param title substring of the description to look for
     * @param request viewing context for rendering the description template
     * @return list of contents of type T
     */
    private <T extends Content> List<T> getObjectsViaDescription(Class<T> c, String title, ServletRequest request) {
        List<T> result = new ArrayList<>();

        if (StringUtils.isNotBlank(title)) {
            List<T> beans = beanFactory.listBeans(c);
            LOG.debug("getObjectsViaDescription({} :{}) checking {}", title, c.getSimpleName(), beans);
            for (T bean : beans) {
                try {
                    LOG.debug("getObjectsViaDescription() checking bean {}", bean);
                    BufferResponse r = new BufferResponse();
                    viewUtilties.render(r.getWriter(), bean, "description", request, r);
                    String description = r.getContents();
                    LOG.debug("getObjectsViaDescription({}) {} has description {}", description.indexOf(title), bean, description);
                    if (description.contains(title)) {
                        result.add(bean);
                    } // if
                } catch (IOException ioe) {
                    LOG.error("getObjectsViaDescription() ignoring element "+bean, ioe);
                } // try/catch
            } // for
        } // if

        LOG.debug("getObjectsViaDescription({}) result={}", title, result);
        return result;
    } // getObjectsViaDescription()


    private <T extends Content> T getObjectViaDescription(Class<T> c, String title, ServletRequest request) {
        T result = null;

        List<T> suggestions = getObjectsViaDescription(c, title, request);
        if (suggestions.size()==1) {
            result = suggestions.get(0);
        } // if

        return result;
    } // getObjectViaDescription()


    /**
     * Create an ID matcher from ID_PATTERN to get ids from input strings.
     *
     * @param idString string which might contain a valid content id
     * @return Matcher instance from given string and ID_PATTERN
     */
    private Matcher createIdMatcher(String idString) {
        return Pattern.compile(Constants.ID_PATTERN).matcher(idString);
    } // createidMatcher()


    private Content getReferenceValue(Class<? extends Content> cls, ServletRequest request, String valueString) {
        Content value;
        Matcher m = createIdMatcher(valueString);
        if (m.find()) {
            valueString = m.group(1);
            LOG.info("getReferenceValue() pattern match result {}", valueString);
            value = beanFactory.getBean(valueString);
        } else {
            LOG.warn("getReferenceValue() we should have checked for selection via description template");
            value = getObjectViaDescription(cls, valueString.trim(), request);
            if (value!=null) {
                LOG.info("getReferenceValue() found a value from description {}", value);
            } // if
        } // if
        return value;
    } // getReferenceValue()


    /**
     * @see PropertyConverter#getStorableObject(org.tangram.content.Content,
     * java.lang.String, java.lang.Class, java.lang.reflect.Type,
     * javax.servlet.ServletRequest)
     */
    @Override
    public Object getStorableObject(Content client, String valueString, Class<? extends Object> cls, Type type, ServletRequest request) {
        Object value = null;
        if (valueString!=null) {
            LOG.debug("getStorableObject() required type is {}", cls.getName());
            if (cls==String.class) {
                value = StringUtils.isNotBlank(valueString) ? valueString : null;
            } else {
                if (cls==Date.class) {
                    try {
                        value = dateFormat.parseObject(valueString);
                    } catch (ParseException pe) {
                        LOG.error("getStorableObject() cannot parse as Date: "+valueString);
                    } // try/catch
                } else {
                    if (cls==Long.class) {
                        value = StringUtils.isNotBlank(valueString) ? Long.parseLong(valueString) : null;
                    } else {
                        if (cls==Integer.class) {
                            value = StringUtils.isNotBlank(valueString) ? Integer.parseInt(valueString) : null;
                        } else {
                            if (cls==Float.class) {
                                value = StringUtils.isNotBlank(valueString) ? Float.parseFloat(valueString) : null;
                            } else {
                                if (cls==Boolean.class) {
                                    value = Boolean.parseBoolean(valueString);
                                } else {
                                    if (cls==Markdown.class) {
                                        value = new Markdown(valueString.toCharArray());
                                    } else {
                                        if (cls==List.class) {
                                            LOG.debug("getStorableObject() splitting {}", valueString);

                                            String[] commaSeparated = valueString.split(",");
                                            List<Object> elements = new ArrayList<>();

                                            ParameterizedType parameterizedType = (type instanceof ParameterizedType) ? (ParameterizedType) type : null;
                                            Class elementClass = Content.class;
                                            if (parameterizedType!=null&&parameterizedType.getActualTypeArguments().length==1) {
                                                Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];

                                                LOG.debug("getStorableObject() actualTypeArgument={}", actualTypeArgument);

                                                if (actualTypeArgument instanceof Class) {
                                                    elementClass = SystemUtils.convert(actualTypeArgument);
                                                }
                                            }

                                            for (String oneValuString : commaSeparated) {
                                                oneValuString = oneValuString.trim();

                                                LOG.debug("getStorableObject() idString={}", oneValuString);

                                                if (StringUtils.isNotBlank(oneValuString)) {
                                                    if (Content.class.isAssignableFrom(elementClass)) {
                                                        Matcher m = this.createIdMatcher(oneValuString);
                                                        if (m.find()) {
                                                            oneValuString = m.group(1);
                                                            Content bean = beanFactory.getBean(oneValuString);

                                                            LOG.info("getStorableObject() pattern match result {} ({})", oneValuString, bean);

                                                            if ((bean!=null)&&((client==null)||(!bean.getId().equals(client.getId())))) {
                                                                if (elementClass.isAssignableFrom(bean.getClass())) {
                                                                    elements.add(bean);
                                                                }
                                                            }
                                                        } else {
                                                            LOG.debug("getStorableObject() parameterizedType={}", parameterizedType);

                                                            List<? extends Content> results = this.getObjectsViaDescription(elementClass, oneValuString, request);
                                                            if (!results.isEmpty()) {
                                                                elements.addAll(results);
                                                            }
                                                        }
                                                    } else {
                                                        elements.add(this.getStorableObject(client, oneValuString, elementClass, request));
                                                    }
                                                }
                                            }

                                            value = elements;
                                        } else {
                                            if (Content.class.isAssignableFrom(cls)) {
                                                Class<? extends Content> cc = SystemUtils.convert(cls);
                                                Content referenceValue = getReferenceValue(cc, request, valueString);
                                                value = (client!=null)&&client.equals(referenceValue) ? null : referenceValue;
                                            } // if
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } // if
        return value;
    } // getStorableObject()


    /**
     * @see PropertyConverter#getStorableObject(org.tangram.content.Content,
     * java.lang.String, java.lang.Class, javax.servlet.ServletRequest)
     */
    @Override
    public Object getStorableObject(Content client, String valueString, Class<? extends Object> cls, ServletRequest request) {
        return getStorableObject(client, valueString, cls, null, request);
    } // getStorableObject()


    /**
     * @see PropertyConverter#isBlobType(java.lang.Class)
     */
    @Override
    public abstract boolean isBlobType(Class<?> cls);


    /**
     * @see PropertyConverter#getBlobLength(java.lang.Object)
     */
    @Override
    public abstract long getBlobLength(Object o);


    /**
     * @see PropertyConverter#isTextType(java.lang.Class)
     */
    @Override
    public abstract boolean isTextType(Class<?> cls);


    /**
     * @see PropertyConverter#createBlob(byte[])
     */
    @Override
    public abstract Object createBlob(byte[] octets);

} // AbstractPropertyConverter
