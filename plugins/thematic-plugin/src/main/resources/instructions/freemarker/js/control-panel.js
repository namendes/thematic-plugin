var selectedtheme="";

function loadPreview(url) {
    var resUrl = url.replace(/ /g, "-");
    document.getElementById("previewPanel").setAttribute("src",resUrl);
    selectedtheme=url;
}

function redirect() {
    document.getElementById("formInput").setAttribute("value",selectedtheme);
    var injector = parent['angular'].element(parent.document.body).injector();
    if (injector) {
        injector.get('$rootScope').$apply(function() {
            var channelService = injector.get('ChannelService');
            channelService.recordOwnChange();
        });
    }
}

window.onload = function() {
    document.getElementById("default_Sel").focus();
}