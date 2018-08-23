<#macro grid_element product>
<article class="list--item">
    <figure>
        <img src="${product.getValue("thumb_image")}" alt="">
        <header>
            <h2>${product.getValue("title")}</h2>
        </header>
        <figcaption class="br-details">
            <#assign str = (product.getValue("description"))?replace("<(?:.|\n)*?>|\\s+", " ", "r")>
            <div class="br-details-trigger">More Details
                <span class="br-details-content">${str}</span>
            </div>
        </figcaption>
    </figure>
</article>
</#macro>