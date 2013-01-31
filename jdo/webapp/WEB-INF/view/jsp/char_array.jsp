<%@page isELIgnored="false" language="java" session="false"
	pageEncoding="UTF-8" %><%@page import="java.util.regex.Pattern"%><%@page
	import="java.util.regex.Matcher"%><%@page import="java.util.List"%><%@page
	import="java.util.ArrayList"%><%@page
	import="org.tangram.view.Utils"%><%@page
	import="org.tangram.content.BeanFactory"%><%@page
	import="org.tangram.view.link.LinkFactory"%><%@page
	import="org.tangram.Constants"%><%
    char[] t = (char[])request.getAttribute(Constants.THIS);
    if (t!=null) {
        // TOOD: this is mostly the same code as for Google App Engine
        BeanFactory beanFactory = Utils.getBeanFactory(request);
        LinkFactory linkFactory = Utils.getLinkFactory(request);
        Pattern p = Pattern.compile("http://[a-zA-Z0-9:]*\"");

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
            String url = "FEHLER\"";
            try {
                url = linkFactory.createLink(request, response, beanFactory.getBean(id), null, null).getUrl()+"\"";
            } catch (Exception e) {
                // What could be do about it?
            } // try/catch
            valueString.replace(starts.get(i), ends.get(i), url);
        } // for
        out.write(valueString.toString());
    } // if
%>