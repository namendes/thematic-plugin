<!doctype html>
<#include "../include/imports.ftl">
<#import "product-grid-element.ftl" as grid>
<@hst.webfile var="link" path="css/thematic.css" />
<link rel="stylesheet" href="${link}">
<head>
    <#if thematic??>
        <title>${thematic.getValue('page_header/title')}</title>
        <meta name="keywords" content="${thematic.getValue('page_header/meta_keywords')}">
        <meta name="description" content="${thematic.getValue('page_header/description')}">
    </#if>
    <div class="container1">
        <@hst.include ref="container1"/>
    </div>
</head>
<body>
<div class="container2">
    <@hst.include ref="container2"/>
</div>
<div class="container">
    <form method="get" id="sort">
        <label>Sort : </label>
        <select name="sort" form="sort" onchange="this.form.submit()">
            <option value="" <#if (sort == "")> selected="selected"</#if>>Relevance</option>
            <option value="price asc" <#if (sort == "price asc")> selected="selected"</#if>>Price Ascending</option>
            <option value="price desc" <#if (sort == "price desc")> selected="selected"</#if>>Price Descending</option>
            <option value="title asc" <#if (sort == "title asc")> selected="selected"</#if>>Title Ascending</option>
            <option value="title desc" <#if (sort == "title desc")> selected="selected"</#if>>Title Descending</option>
        </select>
    </form>
    <br>
    <div class="list">
        <#if resp==true??>
            <#list thematic.getValue('response').getValue('docs').children.collection as newsDoc>
                <@grid.grid_element product=newsDoc/>
            </#list>
        <#else>
            <h1>Failed to Fetch Thematic Page</h1>
            <h3>Check whether this Thematic Page exists or not</h3>
        </#if>
    </div>
    <div
    <nav aria-label="Page navigation example">
        <ul class="pagination justify-content-center">

            <li class="page-item"><a class="page-link" href="#">1</a></li>

        </ul>
    </nav>
    <div class="container3">
    <@hst.include ref="container3"/>
    </div>
</div>
<div class="container4">
    <@hst.include ref="container4"/>
</div>
</body>