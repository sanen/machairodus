<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.machairodus.manager.util.Definition" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<% 
Definition definition;
if((definition = (Definition) request.getAttribute("definition")) == null) { %>
<tiles:insertDefinition name="index" />
<% } else { %>
<tiles:insertDefinition name="<%=definition.value() %>" />
<% } %>