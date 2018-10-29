<html>
    <#include "../include/imports.ftl">
    <@hst.webfile var="controlPanelCss" path="css/control-panel.css" />
    <@hst.webfile var="controlPanelJs" path="js/control-panel.js" />
<head>
    <title>Thematic Control Panel Page</title>
    <link rel="stylesheet" href="${controlPanelCss}">
</head>
<body>

<#assign count=1>
<div class="row" id="main">
    <div class="column1">
        <form method="get">
            <div class="search">
                <input name="theme" type="text" id="searchField" class="search_input" placeholder="Search Thematic" />
                <button type="submit">
                    <#--TODO change to local url-->
                    <img src="https://cdn1.iconfinder.com/data/icons/TWG_Retina_Icons/24/magnifier.png"/>
                </button>
            </div>
            <ul class="results">
                <#list searchResults.children.collection as result>
                    <#if count == 1>
                        <#assign firsturl=result.getValue("theme")>
                    <li class="results_item_${count} selected" onclick="loadPreview('${result.getValue("theme")}')" >
                        <p>${result.getValue("theme")}</p>
                        <p>Revenue - ${result.getValue("revenue")}<br>Visits - ${result.getValue("visits")}</p>
                    </li>
                        <#assign count++>
                    <#else>
                    <li class="results_item_${count}" onclick="loadPreview('${result.getValue("theme")}')">
                        <p>${result.getValue("theme")}</p>
                        <p>Revenue - ${result.getValue("revenue")}<br>Visits - ${result.getValue("visits")}</p>
                    </li>
                        <#assign count++>
                    </#if>
                </#list>
            </ul>
        </form>
    </div>
    <div class="column2">
        <iframe id="previewPanel" src="${firsturl}" style="width: 100%; height: 100%;"></iframe>
    </div>
</div>
<div id="customiseDiv">
    <form action="<@hst.actionURL/>" method="post">
        <p> <input id="formInput" type="text" name="theme"></p>
        <p> <input type="submit" value="Customize" onclick="redirect()"></p>
    </form>
</div>


<script src="${controlPanelJs}"></script>
</body>



</html>
<#-- <div id="spinner" style="position:absolute; top: 0; left: 0; width:200px; height:100px;">
     <div class="svg">
         <svg version="1.1" id="Layer_2" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="-237 328.1 136.9 136.9" style="enable-background:new -237 328.1 136.9 136.9;" xml:space="preserve"><style type="text/css">.st0{fill:none;stroke:#00B1E2;stroke-width:2;stroke-miterlimit:10;}.st1{fill:none;stroke:#00B1E2;stroke-width:2;}</style><g><path class="st0" d="M-168.5,330.5c-35.9,0-65,29.1-65,65c0,20.3,9.3,38.5,23.9,50.4v-20.3v-68.7h17.2v39c5.1-2.9,10.9-4.6,17.2-4.6c0.6,0,1.2,0,1.8,0c0.2,0,0.4,0,0.6,0c0.4,0,0.8,0.1,1.2,0.1c0.2,0,0.4,0.1,0.6,0.1c0.4,0,0.8,0.1,1.1,0.1c0.2,0,0.4,0.1,0.6,0.1c0.4,0.1,0.7,0.1,1.1,0.2c0.2,0,0.4,0.1,0.6,0.1c0.4,0.1,0.7,0.2,1.1,0.3c0.2,0.1,0.4,0.1,0.6,0.2c0.4,0.1,0.7,0.2,1.1,0.3c0.2,0.1,0.4,0.1,0.5,0.2c0.4,0.1,0.7,0.2,1.1,0.4c0.2,0.1,0.3,0.1,0.5,0.2c0.4,0.2,0.8,0.3,1.1,0.5c0.1,0.1,0.3,0.1,0.4,0.2c0.4,0.2,0.8,0.4,1.1,0.5c0.1,0.1,0.3,0.1,0.4,0.2c0.4,0.2,0.8,0.4,1.2,0.6c0.1,0.1,0.2,0.1,0.3,0.2c0.4,0.2,0.8,0.5,1.2,0.7c0.1,0.1,0.2,0.1,0.3,0.2c0.4,0.3,0.8,0.5,1.2,0.8c0.1,0,0.1,0.1,0.2,0.1c0.4,0.3,0.8,0.6,1.2,0.9c0,0,0.1,0.1,0.2,0.1c0.4,0.3,0.8,0.7,1.2,1l0.1,0.1c0.4,0.4,0.8,0.7,1.2,1.1c0,0,0,0,0.1,0c0.4,0.4,0.8,0.8,1.2,1.2l0.1,0.1c5.7,6.1,9.2,14.3,9.2,23.4c0,19-15.4,34.4-34.4,34.4c-1.4,0-10.1-0.3-17.3-4.6c8.6,5.3,20.3,5.2,24,5.2c35.9,0,65-29.1,65-65C-103.5,359.6-132.6,330.5-168.5,330.5z" style="stroke-dasharray: 878.245, 878.245; stroke-dashoffset: 117;"></path><path class="st1" d="M-163.7,413c-0.4-0.4-0.8-0.7-1.3-1c-0.1,0-0.1-0.1-0.2-0.1c-0.4-0.3-0.8-0.6-1.2-0.8c-0.1-0.1-0.2-0.1-0.3-0.2c-0.4-0.2-0.8-0.4-1.2-0.7c-0.1-0.1-0.3-0.1-0.4-0.2c-0.4-0.2-0.8-0.3-1.2-0.5c-0.2-0.1-0.3-0.1-0.5-0.2c-0.4-0.1-0.8-0.2-1.2-0.3c-0.2,0-0.4-0.1-0.5-0.1c-0.4-0.1-0.8-0.2-1.3-0.2c-0.2,0-0.3-0.1-0.5-0.1c-0.6-0.1-1.2-0.1-1.8-0.1c-9.5,0-17.2,7.7-17.2,17.2s7.7,17.2,17.2,17.2s17.2-7.7,17.2-17.2c0-4.3-1.6-8.3-4.2-11.3C-162.7,413.9-163.2,413.4-163.7,413C-163.6,413-163.6,413-163.7,413z" style="stroke-dasharray: 308.263, 308.263; stroke-dashoffset: 41;"></path><path class="st1" d="M-150.1,402.2L-150.1,402.2l-12.3,12.1" style="stroke-dasharray: 217.254, 217.254; stroke-dashoffset: 28;"></path><path class="st1" d="M-192.5,425.6v29.7c2,1.2,4.2,2.1,6.5,2.9" style="stroke-dasharray: 236.83, 236.83; stroke-dashoffset: 31;"></path><path class="st1" d="M-209.7,425.6c0-12.7,6.9-23.8,17.2-29.8" style="stroke-dasharray: 236.026, 236.026; stroke-dashoffset: 31;"></path></g></svg>
     </div>
 </div>-->
