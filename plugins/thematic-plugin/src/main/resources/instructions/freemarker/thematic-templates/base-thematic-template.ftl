<#include "../include/imports.ftl">

hello
<#list news.getValue('response').getValue('docs').children.collection as newsDoc>

<p>${newsDoc.getValue('title')}</p>
<p>${newsDoc.getValue('description')}</p>

</#list>