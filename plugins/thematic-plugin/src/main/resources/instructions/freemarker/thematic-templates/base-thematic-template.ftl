<!doctype html>
<#include "../include/imports.ftl">
<#import "product-grid-element.ftl" as grid>
<@hst.webfile var="link" path="css/thematic.css" />

<body>
<div class="container">
    <div class="list">
        <#if rsp==true??>
            <#list thematic.getValue('response').getValue('docs').children.collection as newsDoc>
                <@grid.grid_element product=newsDoc/>
            </#list>
        <#else>
            <h1>FAILED!!!!!</h1>
        </#if>
    </div>
</div>
<link rel="stylesheet" href="${link}">
</body>