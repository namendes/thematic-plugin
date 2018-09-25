<!doctype html>
<#include "../include/imports.ftl">
<head>
    Thematic Control panel
</head>

<body>




<form action="<@hst.actionURL/>" method="post">
    <p> theme to add to sitemap <input type="text" name="theme"></p>
    <p> add <input type="submit" value="Add theme" onclick="redirect()"></p>
</form>





<#--
<a href="site/thematic/pink%20satin%20top"> here <a/>

    <div id="IframeWrapper" style="position: relative;">
        <iframe  id="iframewebpage" style="z-index:1"  runat="server" src="https://www.forever21.com/us/shop/catalog/category/f21/dress" height="1200" width="800" onload="document.getElementById('spinner').style.display='none';">
        </iframe>
        <div id="spinner" style="position:absolute; top: 0; left: 0; width:200px; height:100px;">
            <div class="svg">
                <svg version="1.1" id="Layer_2" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="-237 328.1 136.9 136.9" style="enable-background:new -237 328.1 136.9 136.9;" xml:space="preserve"><style type="text/css">.st0{fill:none;stroke:#00B1E2;stroke-width:2;stroke-miterlimit:10;}.st1{fill:none;stroke:#00B1E2;stroke-width:2;}</style><g><path class="st0" d="M-168.5,330.5c-35.9,0-65,29.1-65,65c0,20.3,9.3,38.5,23.9,50.4v-20.3v-68.7h17.2v39c5.1-2.9,10.9-4.6,17.2-4.6c0.6,0,1.2,0,1.8,0c0.2,0,0.4,0,0.6,0c0.4,0,0.8,0.1,1.2,0.1c0.2,0,0.4,0.1,0.6,0.1c0.4,0,0.8,0.1,1.1,0.1c0.2,0,0.4,0.1,0.6,0.1c0.4,0.1,0.7,0.1,1.1,0.2c0.2,0,0.4,0.1,0.6,0.1c0.4,0.1,0.7,0.2,1.1,0.3c0.2,0.1,0.4,0.1,0.6,0.2c0.4,0.1,0.7,0.2,1.1,0.3c0.2,0.1,0.4,0.1,0.5,0.2c0.4,0.1,0.7,0.2,1.1,0.4c0.2,0.1,0.3,0.1,0.5,0.2c0.4,0.2,0.8,0.3,1.1,0.5c0.1,0.1,0.3,0.1,0.4,0.2c0.4,0.2,0.8,0.4,1.1,0.5c0.1,0.1,0.3,0.1,0.4,0.2c0.4,0.2,0.8,0.4,1.2,0.6c0.1,0.1,0.2,0.1,0.3,0.2c0.4,0.2,0.8,0.5,1.2,0.7c0.1,0.1,0.2,0.1,0.3,0.2c0.4,0.3,0.8,0.5,1.2,0.8c0.1,0,0.1,0.1,0.2,0.1c0.4,0.3,0.8,0.6,1.2,0.9c0,0,0.1,0.1,0.2,0.1c0.4,0.3,0.8,0.7,1.2,1l0.1,0.1c0.4,0.4,0.8,0.7,1.2,1.1c0,0,0,0,0.1,0c0.4,0.4,0.8,0.8,1.2,1.2l0.1,0.1c5.7,6.1,9.2,14.3,9.2,23.4c0,19-15.4,34.4-34.4,34.4c-1.4,0-10.1-0.3-17.3-4.6c8.6,5.3,20.3,5.2,24,5.2c35.9,0,65-29.1,65-65C-103.5,359.6-132.6,330.5-168.5,330.5z" style="stroke-dasharray: 878.245, 878.245; stroke-dashoffset: 117;"></path><path class="st1" d="M-163.7,413c-0.4-0.4-0.8-0.7-1.3-1c-0.1,0-0.1-0.1-0.2-0.1c-0.4-0.3-0.8-0.6-1.2-0.8c-0.1-0.1-0.2-0.1-0.3-0.2c-0.4-0.2-0.8-0.4-1.2-0.7c-0.1-0.1-0.3-0.1-0.4-0.2c-0.4-0.2-0.8-0.3-1.2-0.5c-0.2-0.1-0.3-0.1-0.5-0.2c-0.4-0.1-0.8-0.2-1.2-0.3c-0.2,0-0.4-0.1-0.5-0.1c-0.4-0.1-0.8-0.2-1.3-0.2c-0.2,0-0.3-0.1-0.5-0.1c-0.6-0.1-1.2-0.1-1.8-0.1c-9.5,0-17.2,7.7-17.2,17.2s7.7,17.2,17.2,17.2s17.2-7.7,17.2-17.2c0-4.3-1.6-8.3-4.2-11.3C-162.7,413.9-163.2,413.4-163.7,413C-163.6,413-163.6,413-163.7,413z" style="stroke-dasharray: 308.263, 308.263; stroke-dashoffset: 41;"></path><path class="st1" d="M-150.1,402.2L-150.1,402.2l-12.3,12.1" style="stroke-dasharray: 217.254, 217.254; stroke-dashoffset: 28;"></path><path class="st1" d="M-192.5,425.6v29.7c2,1.2,4.2,2.1,6.5,2.9" style="stroke-dasharray: 236.83, 236.83; stroke-dashoffset: 31;"></path><path class="st1" d="M-209.7,425.6c0-12.7,6.9-23.8,17.2-29.8" style="stroke-dasharray: 236.026, 236.026; stroke-dashoffset: 31;"></path></g></svg>
            </div>
        </div>
        <div id="iframeBlocker" style="position:absolute; top: 0; left: 0; width:95%; height:95%;background-color:aliceblue;opacity:0.1;"></div>
    </div>

    <button onclick="document.getElementById('iframewebpage').src='https://www.forever21.com/us/shop/catalog/category/f21/dress_casual';document.getElementById('spinner').style.display='block';">src 1</button>


    <button onclick="document.getElementById('iframewebpage').src='https://www.forever21.com/us/shop/catalog/category/f21/dress_wrap'; document.getElementById('spinner').style.display='block';"">src 2</button>
-->



    <script type="text/javascript">
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
        draw();

        var redirect = function () {
            var injector = parent['angular'].element(parent.document.body).injector();
            if (injector) {
                injector.get('$rootScope').$apply(function() {
                    var channelService = injector.get('ChannelService');
                    channelService.recordOwnChange();
                });
            }
        }



    </script>

</body>