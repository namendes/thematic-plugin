<html>
    <#include "../include/imports.ftl">
    <#import "product-grid-element.ftl" as grid>
    <@hst.webfile var="thematicStyle" path="css/thematic.css" />
    <@hst.webfile var="thematicJs" path="js/thematic.js" />
<head>
        <#if thematic??>
            <title>${thematic.getValue('page_header/title')}</title>
            <meta name="keywords" content="${thematic.getValue('page_header/meta_keywords')}">
            <meta name="description" content="${thematic.getValue('page_header/meta_description')}">
        </#if>
</head>
<body>
    <#if leftNav != "">
    <div class="sidenav">
        <#if leftNav.getValue("heading")??>
            <a href="#" style="color:white;">${leftNav.getValue("heading")}</a>
            <#if leftNav.getValue("attribute_groups")??>
                <#list leftNav.getValue("attribute_groups").children.collection as attrGroup>
                    <#if attrGroup.getValue("heading")??>
                    <a href="#" style="color:white;margin-left: 10px;">${attrGroup.getValue("heading")}</a>
                        <#list attrGroup.getValue("links").children.collection as attrLink>
                        <a href="${attrLink.getValue("url")}" style="margin-left: 20px;">${attrLink.getValue("anchor_text")}</a>
                        </#list>
                    </#if>
                </#list>
            </#if>
        </#if>
    </div>
    </#if>
<div class="main">
    <div class="container1">
            <@hst.include ref="container1"/>
    </div>
    <div class="container2">
            <@hst.include ref="container2"/>
    </div>
    <div class="container">
        <#if sort??>
            <form method="get" id="sort">
                <label>Sort : </label>
                <select name="sort" form="sort" onchange="this.form.submit()">
                    <option value="" <#if (sort == "")> selected="selected"</#if>>Relevance</option>
                    <option value="price asc" <#if (sort == "price asc")> selected="selected"</#if>>Price Ascending
                    </option>
                    <option value="price desc" <#if (sort == "price desc")> selected="selected"</#if>>Price Descending
                    </option>
                    <option value="title asc" <#if (sort == "title asc")> selected="selected"</#if>>Title Ascending
                    </option>
                    <option value="title desc" <#if (sort == "title desc")> selected="selected"</#if>>Title Descending
                    </option>
                </select>
            </form>
        </#if>
        <div class="list">
            <#if thematic??>
                <#list thematic.getValue('response/docs').children.collection as newsDoc>
                    <@grid.grid_element product=newsDoc/>
                </#list>
            <#else>
                <h1>Failed to Fetch Thematic Page</h1>
                <h3>Check whether this Thematic Page exists or not</h3>
            </#if>
        </div>
        <div class="pagination">
                <#if (currentPage != 1)>
                <#-- First Page is always indexed from 1 -->
                    <a onclick="redirectPage('1')">First</a>
                    <a onclick="redirectPage(${currentPage-1})">${currentPage-1}</a>
                </#if>
            <a class="active">${currentPage}</a>
                <#if (currentPage != totalPages)>
                    <a onclick="redirectPage(${currentPage+1})">${currentPage+1}</a>
                <#-- Last Page is always indexed at totalPages -->
                    <a onclick="redirectPage(${totalPages})">Last</a>
                </#if>
        </div>
        <div class="container3">
                <@hst.include ref="container3"/>
        </div>
    </div>
    <div class="container4">
            <@hst.include ref="container4"/>
    </div>
</div>
</body>
<link rel="stylesheet" href="${thematicStyle}">
<script src="${thematicJs}"></script>
<#if hstRequest.requestContext.cmsRequest>
<script>
    top.addEventListener("message", receiveMessage);
    function receiveMessage(event)
    {
        if(event.data.indexOf("reload.") > -1){
            console.log("reload ", event.data.split(".")[1]);
            window.location.href = "<@hst.link siteMapItemRefId="thematic"/>/"+event.data.split(".")[1];
        }
    }
</script>
</#if>

</html>