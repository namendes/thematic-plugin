<!doctype html>
<#include "../include/imports.ftl">
<#import "product-grid-element.ftl" as grid>

<#global  prefix = "test_data" />
<#global  lpm_top_container = "${prefix}-top-container" />
<#global  lpm_h1 = "${prefix}-header" />
<#global  lpm_product_grid = "${prefix}-product-grid" />
<#global  lpm_product_prefix = "${prefix}-product" />
<#global  lpm_product_url = "${prefix}-product-url" />
<#global  lpm_product_image = "${prefix}-product-image" />
<#global  lpm_product_heading = "${prefix}-product-heading" />
<#global  lpm_product_brand = "${prefix}-product-brand" />
<#global  lpm_product_desc = "${prefix}-product-desc" />
<#global  lpm_product_cost = "${prefix}-product-cost" />
<#global  lpm_product_salecost = "${prefix}-product-salecost" />
<#global  lpm_popup = "${prefix}-popup" />
<#global  lpm_popup_url = "${prefix}-popup-url" />
<#global  lpm_popup_image = "${prefix}-popup-image" />
<#global  lpm_popup_heading = "${prefix}-popup-heading" />
<#global  lpm_popup_brand = "${prefix}-popup-brand" />
<#global  lpm_popup_desc = "${prefix}-popup-desc" />
<#global  lpm_popup_cost = "${prefix}-popup-cost" />
<#global  lpm_popup_salecost = "${prefix}-popup-salecost"/>
<#global  thematicpage_result_index = 0/>
<#global  themeblocklist_result_index = 0/>
<#global  themeblock_result_index = 0/>
<#global  resultlist_result_index = 0/>
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
<#list thematic.getValue('response').getValue('docs').children.collection as newsDoc>
    <#global thematicpage_result_index = thematicpage_result_index + 1 />
    <#global themeblocklist_result_index = themeblocklist_result_index + 1 />
    <#global themeblock_result_index = themeblock_result_index + 1 />
    <#global resultlist_result_index = resultlist_result_index + 1 />
    <#global lpm_product = "prefix__hashmd5" />
    <@grid.grid_element product=newsDoc/>
</#list>
    </div>
    <div class="container3">
    <@hst.include ref="container3"/>
    </div>
</div>
<div class="container4">
    <@hst.include ref="container4"/>
</div>
</body>