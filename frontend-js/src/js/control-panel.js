function loadPreview(url) {
    UiExtension.register().then((ui) => {
        top.postMessage("reload." + url, "*");
    });
}

function reloadPanel(ui, theme){
    var oReq = new XMLHttpRequest();
    oReq.open("POST", ui.baseUrl+"ws/thematic/thematicpages/search/"+theme+"/page/aaa");
    oReq.setRequestHeader("Content-Type", "application/json");
    oReq.withCredentials = true;
    oReq.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {

            var thematicPages = JSON.parse(this.responseText);
            document.getElementById("search_result").innerHTML = "";
            for (var i = 0; i < thematicPages.length; i++) {
                var searchResultHTML =
                    "<li class=\"rippleLink results_item results_item_"+i+"\" onclick=\"loadPreview('"+thematicPages[i]["theme"]+"')\" >" +
                    "<div class=\"result_theme\">"+thematicPages[i]["h1_default"]+" <br> " +
                    "<div class=\"customise_tag\"> <!--TODO--></div>" +
                    "</div>" +
                    "<div class=\"result_details\">Revenue: "+thematicPages[i]["revenue"]+"<br>Visits: "+thematicPages[i]["visits"]+"</div>" +
                    "</li>";

                document.getElementById("search_result").innerHTML += searchResultHTML;
            }
        }
        else{
            document.getElementById("search_result").innerHTML = "Something went wrong. Check if the backend is down or your credentials are correct";
        }
    };
    var uiExtensionConfig = JSON.parse(ui.extension.config);
    oReq.send(JSON.stringify(uiExtensionConfig["thematicPanelConfig"]));
}

function customize(ui, theme){

    ui.channel.page.get().then((page) => {
        var oReq = new XMLHttpRequest();

        oReq.open("POST", ui.baseUrl+"ws/thematic/thematicpages/customize/"+theme+"/page/"+page.siteMapItem.id);
        oReq.setRequestHeader("Content-Type", "application/json");
        oReq.withCredentials = true;
        oReq.setRequestHeader("contextPath", "/site");
        oReq.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                ui.channel.refresh();
            }

        };
        var uiExtensionConfig = JSON.parse(ui.extension.config);
        oReq.send(JSON.stringify(uiExtensionConfig["thematicPanelConfig"]));
    });
}

UiExtension.register().then((ui) => {
    document.getElementById("search_form").addEventListener("submit", function (event) {
        var search_value = document.getElementById("search_value").value;
        reloadPanel(ui, search_value);
        event.preventDefault();
    });

    document.getElementById("customize_form").addEventListener("submit", function (event) {
        ui.channel.page.get().then((page) => {
            var urlTokens = page.url.split("/");
            customize(ui, urlTokens[urlTokens.length - 1]);
        });
        event.preventDefault();
    });

    reloadPanel(ui, "%20");
});

var links = document.querySelectorAll('.rippleLink');

for (var i = 0, len = links.length; i < len; i++) {
    links[i].addEventListener('click', function (e) {
        var targetEl = e.target;
        var inkEl = targetEl.querySelector('.ink');

        if (inkEl) {
            inkEl.classList.remove('animate');
        } else {
            inkEl = document.createElement('span');
            inkEl.classList.add('ink');
            inkEl.style.width = inkEl.style.height = Math.max(targetEl.offsetWidth, targetEl.offsetHeight) + 'px';
            targetEl.appendChild(inkEl);
        }

        inkEl.style.left = (e.offsetX - inkEl.offsetWidth / 2) + 'px';
        inkEl.style.top = (e.offsetY - inkEl.offsetHeight / 2) + 'px';
        inkEl.classList.add('animate');
    }, false);
}

