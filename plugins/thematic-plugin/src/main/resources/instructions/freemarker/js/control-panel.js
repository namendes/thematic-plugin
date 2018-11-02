var selectedtheme="";

function loadPreview(url, className) {
    var resUrl = url.replace(/ /g, "-");
    document.getElementById("previewPanel").setAttribute("src",resUrl);
    document.getElementsByClassName("selected")[0].classList.add("results_item");
    document.getElementsByClassName("selected")[0].classList.remove("selected");
    document.getElementsByClassName(className)[0].classList.remove("results_item");
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


var links = document.querySelectorAll('.ripplelink');

for (var i = 0, len = links.length; i < len; i++) {
    links[i].addEventListener('click', function(e) {
        var targetEl = e.target;
        var inkEl = targetEl.querySelector('.ink');

        if (inkEl) {
            inkEl.classList.remove('animate');
        }
        else {
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
