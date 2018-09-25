<!doctype html>
<#include "../include/imports.ftl">
<#import "product-grid-element.ftl" as grid>
<@hst.webfile var="link" path="css/thematic.css" />
<link rel="stylesheet" href="${link}">
<head>
    <div class="container1">
    <@hst.include ref="container1"/>
    </div>
</head>
<body>
<div class="container2">
    <@hst.include ref="container2"/>
</div>
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
    <div class="container3">
    <@hst.include ref="container3"/>
    </div>
</div>
<div class="container4">
    <@hst.include ref="container4"/>
</div>
<link rel="stylesheet" href="${link}">
</body>