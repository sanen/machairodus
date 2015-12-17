<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<% if(request.getAttribute("definition") == null) { %>
<tiles:insertDefinition name="index" />
<% } else { %>
<tiles:insertDefinition name="${definition}" />
<% } %>