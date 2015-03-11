<%
        /**
         * Copyright (c) 2015 CSC        
         
         * This library is distributed in the hope that it will be useful, but WITHOUT
         * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
         * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
         * details. usage:
         wget "http://avaa.tdata.fi/smart-smear-portlet/tablemetadata.jsp?id=11
"
         */
%><%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><portlet:defineObjects
 /><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8" 
 %><%@ page import="javax.portlet.PortletPreferences"
 %><%@ page import="com.liferay.portal.kernel.dao.orm.DynamicQuery, 
        com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil,
        com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil,
        com.liferay.portal.kernel.exception.SystemException,
        
        fi.csc.smear.db.model.SmearTableMetadata,
        fi.csc.smear.db.service.SmearTableMetadataLocalServiceUtil,
        java.util.List,
        com.google.gson.Gson"%>
                <% 
                 DynamicQuery query = DynamicQueryFactoryUtil.forClass(SmearTableMetadata.class)
                                .add(RestrictionsFactoryUtil.eq("tableID",
                                		Long.parseLong(request.getParameter("id"))));
                List<SmearTableMetadata> data = SmearTableMetadataLocalServiceUtil.dynamicQuery(query);
                Gson gson = new Gson();
                out.println(gson.toJson(data)); 
                %>
