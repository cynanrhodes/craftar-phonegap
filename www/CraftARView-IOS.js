(function(){
 
window.craftarjs = {
    
startFinding: function(){
    window.location = "craftar://startFinding";
},
 
stopFinding: function(){
    window.location = "craftar://stopFinding";
},
    
takePictureAndSearch: function(){
    window.location = "craftar://takePictureAndSearch";
},
 
triggerFocus: function(){
	//Not implemented
},

restartCamera: function(){
    window.location = "craftar://restartCamera";
},
    
setToken: function(tkn){
    window.location = "craftar://setToken?p=" + encodeURI(tkn);
},
    
    
stopAR: function(){
    window.location = "craftar://stopAR";
},
    
    
setAutomaticAR: function(autoar){
    window.location = "craftar://setAutomaticAR?p=" + autoar;
},
    
close:function(){
    window.location = "craftar://close";
}
    
};

})();
