<#include "../include/imports.ftl">

<h1>Thematic</h1>
<#list news.getValue('response').getValue('docs').children.collection as newsDoc>

<p>${newsDoc.getValue('description')}</p>

</#list>
