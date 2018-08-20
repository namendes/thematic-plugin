<#macro grid_element product>
<li class="category-item $lpm-product" id="${product.getValue("pid")}Overlay">
    <figure class="product-thumbnail">
        <div class="product-image-frame">
            <a class="product-thumbnail-image ${lpm_product_url}" href="${product.getValue("url")}">
                <img class="product-thumbnail-image $lpm-product-image" title="${product.getValue("title")}" style="" src="$image" id="${product.getValue("pid")}" alt="" data-alt-image="">
            </a>
            <a class="quick-look" onmousedown="javascript:hideBrPop('popupBR');" onmouseup="document.getElementById('br-${thematicpage_result_index}').style.display = 'block';" href="javascript:void(0);" >Quick Look</a>
        </div>
        <figcaption class="product-description">
            <div class="productdesigner">
                <a class="recordTextLink ${lpm_product_url} ${lpm_product_brand}" href="${product.getValue("url")}">${product.getValue("brand")}</a>
            </div>
            <div class="productname hasdesigner">
                <a class="recordTextLink ${lpm_product_heading} ${lpm_product_url}" onclick="s_objectID=&quot;${product.getValue("url")}&quot;;return this.s_oc?this.s_oc(e):true" href="${product.getValue("url")}">${product.getValue("title")}</a>
            </div>

            <div class="br-details">
                <span class="br-details-trigger">More Details</span>
                <span class="br-details-content">${product.getValue("description")}</span>
            </div>

            <#assign show_salecost = false/>
            <#if (product.hasValue("sale_price") && product.hasValue("price") && !(product.getValue("sale_price") == product.getValue("price"))) >
                <#assign show_salecost = true/>
            </#if>
            <#if show_salecost>
            <div class="product-price">
                <p class="price-adornment priceadorn strikethrough">
                    <span class="price-adornment-label">Original:&nbsp;</span>
                    <span class="price ${lpm_popup_cost}">${product.getValue("price")}</span>
                </p>
                <p class="price-adornment priceadorn price-adornment-highlight ">
                    <span class="price-adornment-label">NOW:&nbsp;</span>
                    <span class="price ${lpm_popup_salecost}">${product.getValue("sale_price")}</span>
                </p>
            </div>
            <#else>
            <div class="product-price ${lpm_popup_salecost}">${product.getValue("sale_price")}</div>
            </#if>

        </figcaption>
    </figure>

    <div class="lightBox">
        <!--[popup]-->
        <div class="popupBR ${lpm_popup}" id="br-${thematicpage_result_index}">
            <div class="popupInner">
                <a class="xclose" onmousedown="document.getElementById('br-${thematicpage_result_index}').style.display = 'none';" href="#"></a>

                <div class="pop_Left">
                    <a href="${product.getValue("url")}" class="br_mainblock_${thematicpage_result_index}_url ${lpm_popup}-url">
                        <img src="${product.getValue("thumb_image")}" alt="${product.getValue("title")}" class="br_mainblock_${thematicpage_result_index}_image ${lpm_popup}-image" />
                    </a>
                </div>
                <div class="pop_Right">
                    <div class="productDesignerName"><a href="${product.getValue("url")}" class="br_mainblock_${thematicpage_result_index}_brand ${lpm_popup}-url ${lpm_popup}-brand">${product.getValue("brand")}</a></div>
                    <div class="productName"><a href="${product.getValue("url")}" class="br_mainblock_${thematicpage_result_index}_heading ${lpm_popup}-heading ${lpm_popup}-url">${product.getValue("title")}</a></div>
                    <div class="priceElement">
                        <#if show_salecost>
                            <div class="adornmentPriceElement">
                                <div class="adorn label pos2">ORIGINAL:</div> <div class="price pos2 ${lpm_popup_cost}">${product.getValue("price")}</div>
                            </div>
                            <div class="adornmentPriceElement">
                                <div class="adorn label pos1">NOW:</div> <div class="price pos1 ${lpm_popup_salecost}"> ${product.getValue("sale_price")}</div>
                                <div class="maxclear"></div>
                            </div>
                        <#else>
                        <div class="originalPriceElement">
                            <div class="price ${lpm_popup_cost}">${product.getValue("price")}</div>
                        </div>
                        <div class="maxclear"></div>
                        </#if>
                    </div>
                    <div class="br_mainblock_${thematicpage_result_index}_desc ${lpm_popup}-desc qvBorder">${product.getValue("description")}</div>
                    <p class="more"><a href="${product.getValue("url")}" class="br_mainblock_${thematicpage_result_index}_url ${lpm_popup}-url">Read Full Details on Item Page</a></p>
                </div>

            </div>
        </div>
        <!--[/popup]-->
    </div>

</li>
</#macro>

<script type="text/javascript">
    function hideBrPop(theClass) {
        var allPageClasses = document.getElementsByClassName(theClass);
        for (var i = allPageClasses.length; i--;) {
            allPageClasses[i].style.display='none';
        }
    }
    document.onkeydown = function(e) {
        var KEYCODE_ESC = 27;
        if (!e) {
            e = window.event;
        }
        if (e.keyCode === KEYCODE_ESC) {
            hideBrPop('_popup');
            document.body.removeAttribute('style');
        }
    };
</script>
