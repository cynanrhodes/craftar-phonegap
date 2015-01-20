(function(){

    if (!navigator.userAgent.toLowerCase().match(/android/)){
        var js = document.createElement('script');
        js.type = "text/javascript";
        js.src = "platforms/CraftARView-IOS.js";
        var h = document.getElementsByTagName('head')[0];
        h.appendChild(js);
    }

})();

