<%@page isELIgnored="false" language="java" session="false" pageEncoding="UTF-8" 
%><%@page import="java.util.regex.Pattern,java.util.regex.Matcher"
%><%@page import="java.util.List,java.util.ArrayList"
%><%@page import="org.tangram.Constants,org.tangram.view.Utils"
%><%@page import="org.tangram.content.BeanFactory,org.tangram.link.LinkFactoryAggregator"
%><%
    char[] t = (char[])request.getAttribute(Constants.THIS);
    if (t!=null) {
        String view = (String)request.getAttribute(Constants.ATTRIBUTE_EMBDDED_VIEW);
        String action = (String)request.getAttribute(Constants.ATTRIBUTE_EMBEDDED_ACTION);
        BeanFactory beanFactory = (LinkFactoryAggregator)application.get(Constants.ATTRIBUTE_BEAN_FACTORY_AGGREGATOR);
        LinkFactoryAggregator linkFactory = (LinkFactoryAggregator)application.get(Constants.ATTRIBUTE_LINK_FACTORY_AGGREGATOR);
        Pattern p = Constants.TEXT_ID_PATTERN;
        StringBuffer valueString = new StringBuffer(new String(t));
        Matcher m = p.matcher(valueString);
        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        while (m.find()) {
            starts.add(m.start());
            ends.add(m.end());
        } // while
        for (int i = starts.size(); i>0;) {
            i-- ;
            String raw = valueString.substring(starts.get(i), ends.get(i));
            String id = raw.substring(7, raw.length()-1);
            String url = "ERROR\"";
            try {
                url = linkFactory.createLink(request, response, beanFactory.getBean(id), action, view).getUrl()+"\"";
            } catch (Exception e) {
                // What could be do about it?
            } // try/catch
            valueString.replace(starts.get(i), ends.get(i), url);
        } // for
        out.write(valueString.toString());
    } // if
%>