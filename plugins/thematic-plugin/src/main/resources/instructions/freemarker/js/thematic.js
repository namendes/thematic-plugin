function redirectPage(page) {
    var url = window.location.href;
    if (url.indexOf('?') > -1){
        if(url.indexOf('page=') > -1){
            url = url.replace(/page=[0-9]+/,"page="+page)
        }else{
            url += '&page='+page;
        }
    }else{
        url += '?page='+page;
    }
    window.location.href = url;
};