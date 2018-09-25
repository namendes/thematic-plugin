var selectedtheme="";

function loadPreview(url) {
    document.getElementById("previewPanel").setAttribute("src",url);
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

function getTheme() {
    return selectedtheme;
}