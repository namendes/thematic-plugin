var selectedtheme="";

function loadPreview(url, className) {
    var resUrl = url.replace(/ /g, "-");
    document.getElementById("previewPanel").setAttribute("src",resUrl);
    document.getElementsByClassName("selected")[0].classList.remove("selected");
    document.getElementsByClassName(className)[0].classList.add("selected");
    selectedtheme=url;
}

function redirect() {
    document.getElementById("formInput").setAttribute("value",selectedtheme);
    var injector = parent['angular'].element(parent.document.body).injector();
    if (injector) {
        injector.get('$rootScope').$apply(function() {
            var channelService = injector.get('ChannelService');
            channelService.reload();
        });
    }
}

window.onload = function() {
    document.getElementById("default_Sel").focus();
};