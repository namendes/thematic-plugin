var selectedtheme = "";

function loadPreview(url, urlPath, className) {
    UiExtension.register().then((ui) => {
        top.postMessage("reload."+url, "*");

    var resUrl = url;//.replace(/ /g, "-");
        document.getElementById("previewPanel").setAttribute("src", urlPath + resUrl);
        document.getElementsByClassName("selected")[0].classList.add("results_item");
        document.getElementsByClassName("selected")[0].classList.remove("selected");
        document.getElementsByClassName(className)[0].classList.remove("results_item");
        document.getElementsByClassName(className)[0].classList.add("selected");
        selectedtheme = url;
    });
}

window.onload = function () {
    //document.getElementById("default_Sel").focus();
};

function reloadPanel(ui, theme){
    var oReq = new XMLHttpRequest();
    oReq.open("POST", "/cms/ws/thematic/thematicpages/search/"+theme+"/page/aaa");
    oReq.setRequestHeader("Content-Type", "application/json");
    oReq.withCredentials = true;
    oReq.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {

            var thematicPages = JSON.parse(this.responseText);
            document.getElementById("search_result").innerHTML = "";
            for (var i = 0; i < thematicPages.length; i++) {
                var searchResultHTML =
                    "<li class=\"rippleLink results_item results_item_"+i+"\" onclick=\"loadPreview('"+thematicPages[i]["theme"]+"', '${urlPath}', 'results_item_${count}')\" >" +
                    "<div class=\"result_theme\">"+thematicPages[i]["h1_default"]+" <br> " +
                    "<div class=\"customise_tag\"> TODO</div>" +
                    "</div>" +
                    "<div class=\"result_details\">Revenue: "+thematicPages[i]["revenue"]+"<br>Visits: "+thematicPages[i]["visits"]+"</div>" +
                    "</li>";

                document.getElementById("search_result").innerHTML += searchResultHTML;
            }
        }

    };
    oReq.send(ui.extension.config);
}

function customize(ui, theme){

    ui.channel.page.get().then((page) => {
        var oReq = new XMLHttpRequest();

        oReq.open("POST", "/cms/ws/thematic/thematicpages/customize/"+theme+"/page/"+page.siteMapItem.id);
        oReq.setRequestHeader("Content-Type", "application/json");
        oReq.withCredentials = true;
        oReq.setRequestHeader("contextPath", "/site");
        oReq.onreadystatechange = function() {
            if (this.readyState == 4 && this.status == 200) {
                ui.channel.refresh();
            }

        };
        oReq.send(ui.extension.config);
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


function search(event){
    UiExtension.register().then((ui) => {
        reloadPanel(ui, "camera");
    });

}
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


function ready(id) {
    document.getElementById(id).style.display = "block";
}


function load() {
    adjustSize();

    var iframeDocument = document.getElementById("previewPanel").contentDocument;
    var overlay = iframeDocument.createElement('div');
    overlay.style.position = 'fixed';
    overlay.style.width = '100%';
    overlay.style.height = '100%';
    overlay.style.zIndex = '100000';
    overlay.style.top = 0;
    overlay.style.left = 0;

    iframeDocument.body.appendChild(overlay);
}

function adjustSize() {
    var iframe = document.getElementById("previewPanel");

    if (iframe) {
        iframe.style.width = `${document.body.clientWidth}px`;
    }
}

window.addEventListener('resize', adjustSize);

/*

var current_frame = 0;
var total_frames = 60;
var paths = document.getElementsByTagName('path');
var length = [];

for(var i=0; i< paths.length ;i++){
    var l = paths[i].getTotalLength() + 200;
    length[i] = l;
    paths[i].style.strokeDasharray = l + ' ' + l;
    paths[i].style.strokeDashoffset = l;
}
var handle = 0;

var draw = function() {
    var progress = current_frame/total_frames;
    if (progress > 1) {
        window.cancelAnimationFrame(handle);
    } else {
        current_frame++;
        for(var j=0; j<paths.length;j++){
            paths[j].style.strokeDashoffset = Math.floor(length[j] * (1 - progress));
        }
        handle = window.requestAnimationFrame(draw);
    }
    if (current_frame == 61){ current_frame = 0};
};
draw();*/
