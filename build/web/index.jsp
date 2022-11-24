<%-- 
    Document   : index
    Created on : Apr 4, 2013
    Author     : chmelarp
    Copyright (C) 2013  Petr Chmelar
--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

    <jsp:useBean id="processBean" scope="session" class="net.chmelab.beans.ProcessBean" />
<%  // process the form and session-related stuff first
    // <!-- TODO: enctype="multipart/form-data" -->
    request.setCharacterEncoding("UTF-8");  // JavaEE bastard
    
    processBean.setRequest(request);

    // persistent Commons http://www.jguru.com/faq/view.jsp?EID=744074
    if (session.getAttribute("Commons") == null) {
       session.setAttribute("Commons", processBean.getCommons(request.getLocale()));
    } 
    else {
        processBean.setCommons((net.chmelab.kwdemo.Commons)session.getAttribute("Commons"));
    }
%>
    <jsp:setProperty name="processBean" property="text" />
    <jsp:setProperty name="processBean" property="language" />
<%
    // Brew the page; party's commin
    processBean.main();
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="Copyright" content="(C) 2013  Petr Chmelar">        
        <link rel="stylesheet" type="text/css" href="style.css">
        <title>KeyworDemo</title>        
    </head>
    <body>
        <div style="float:right;"><a href="about.jsp">about</a></div>
        
        <h3>&nbsp;KeyworDemo</h3>


        <form name="PostProcess" action="index.jsp" method="POST">
            <table border="0" cellpadding="2">
                <tbody>
                    <tr>
                        <td colspan="2">
                            <textarea name="text" rows="10" cols="100" maxlength="1000000" autofocus><jsp:getProperty name="processBean" property="text" /></textarea>
                        </td>
                    </tr>                    <tr>
                        <td><input type="submit" value="Process" name="process" /></td>
                        <td style="text-align: right;"> 
<%
                            out.println(processBean.languageSelect());
%>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>

        <table border="0" cellpadding="2">
            <tbody>
                <tr>
                    <td>
                        <jsp:getProperty name="processBean" property="page" />
                    </td>
                </tr>                          
            </tbody>
        </table>

    </body>
</html>
