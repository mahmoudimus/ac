/*! AUI Flat Pack - version 5.0-m17 - generated 2012-11-14 02:21:29 -0500 */


(function(g){var e=g(document),i=(jQuery.browser.msie&&parseInt(jQuery.browser.version,10)==8);var c=(function(){var m=false;function k(p){if(!m&&p.which===1){m=true;e.bind("mouseup mouseleave",l);g(this).trigger("aui-button-invoke")}}function l(){e.unbind("mouseup mouseleave",l);setTimeout(function(){m=false},0)}function o(){if(!m){g(this).trigger("aui-button-invoke")}}function n(p){p.preventDefault()}if(typeof document.addEventListener==="undefined"){return{click:o,"click selectstart":n,mousedown:function(r){k.call(this,r);var s=this;var q=document.activeElement;if(q!==null){q.attachEvent("onbeforedeactivate",p);setTimeout(function(){q.detachEvent("onbeforedeactivate",p)},0)}function p(t){switch(t.toElement){case null:case s:case document.body:case document.documentElement:t.returnValue=false}}}}}return{click:o,"click mousedown":n,mousedown:k}})();var a={"aui-button-invoke":function(H){var l=g(b(this));var k=g(this).addClass("active");var L=l.parent()[0];var O=l.next()[0];var E=g(this).attr("data-dropdown2-hide-location");if(E){var z=document.getElementById(E);if(z){L=g(z);O=undefined}else{throw new Error("The specified data-dropdown2-hide-location id doesn't exist")}}var u=k.closest(".aui-dropdown2-trigger-group");v(l.find("a").first());var n={click:function(){if(!g(this).hasClass("interactive")){y()}},mousemove:function(){v(g(this))}};var r={"click focusin mousedown":function(Q){var R=Q.target;if(!d(R,l[0])&&!d(R,k[0])){y()}},keydown:function(R){if(R.shiftKey&&R.keyCode==9){J(-1)}else{switch(R.keyCode){case 13:var Q=l.find("a.active")[0];if(Q){j(Q)}break;case 27:y();break;case 37:P(-1);break;case 38:J(-1);break;case 39:P(1);break;case 40:J(1);break;case 9:J(1);break;default:return}}R.preventDefault()}};function B(Q,R){Q.each(function(){var S=g(this);S.attr("role",R);if(S.hasClass("checked")){S.attr("aria-checked","true");if(R=="radio"){S.closest("ul").attr("role","radiogroup")}}else{S.attr("aria-checked","false")}})}k.attr("aria-controls",k.attr("aria-owns"));if(i){l.removeClass("aui-dropdown2-tailed")}l.find(".disabled").attr("aria-disabled","true");l.find("li.hidden > a").addClass("disabled").attr("aria-disabled","true");B(l.find(".aui-dropdown2-checkbox"),"checkbox");B(l.find(".aui-dropdown2-radio"),"radio");l.appendTo(document.body);var s=k.offset();var x=k.outerWidth();var o=l.outerWidth();var t=g("body").outerWidth(true);var D=Math.max(parseInt(l.css("min-width"),10),x);var w=k.data("container")||false;var m="left";if(i){var F=parseInt(l.css("border-left-width"),10)+parseInt(l.css("border-right-width"),10);o=o-F;D=D-F}l.css({display:"block",top:s.top+k.outerHeight()+"px","min-width":D+"px"}).attr("aria-hidden","false");var p=s.left;if(t<p+o&&o<=p+x){p+=x-o;m="right"}if(w){var C=k.closest(w),G=C.offset().left+C.outerWidth(),q=k.offset().left+k.outerWidth(),N=q+o;if(D>=o){o=D}if(N>q){p=q-o;m="right"}if(i){p-=F}}if(k.hasClass("toolbar-trigger")){l.addClass("aui-dropdown2-in-toolbar")}if(k.parent().hasClass("aui-buttons")){l.addClass("aui-dropdown2-in-buttons")}if(k.parents().hasClass("aui-header")){l.addClass("aui-dropdown2-in-header")}l.attr("data-dropdown2-alignment",m);l.css("left",p+"px");l.trigger("aui-dropdown2-show");I("on");function y(){I("off");setTimeout(function(){l.css("display","none").css("min-width","").insertAfter(k).attr("aria-hidden","true");k.removeClass("active");l.removeClass("aui-dropdown2-in-toolbar");l.removeClass("aui-dropdown2-in-buttons");if(O){l.insertBefore(O)}else{l.appendTo(L)}l.trigger("aui-dropdown2-hide")},0)}function v(Q){l.find("a.active").removeClass("active");Q.addClass("active")}function J(Q){v(K(l.find("a:not(.disabled)"),Q,true))}function A(Q){if(Q.length>0){y();Q.trigger("aui-button-invoke")}}function P(Q){A(K(u.find(".aui-dropdown2-trigger:not([aria-disabled=true])"),Q,false))}function K(S,T,R){var Q=S.index(S.filter(".active"));Q+=(Q<0&&T<0)?1:0;Q+=T;if(R){Q%=S.length}else{if(Q<0){Q=S.length}}return S.eq(Q)}function M(){A(g(this))}function I(R){var S="bind";var Q="delegate";if(R!=="on"){S="unbind";Q="undelegate"}e[S](r);u[Q](".aui-dropdown2-trigger:not(.active)","mousemove",M);k[S]("aui-button-invoke",y);l[Q]("a:not(.disabled)",n)}},mousedown:function(k){if(k.which===1){g(this).bind(f)}}};var f={mouseleave:function(){e.bind(h)},"mouseup mouseleave":function(){g(this).unbind(f)}};var h={mouseup:function(k){var l=g(k.target).closest(".aui-dropdown2 a, .aui-dropdown2-trigger")[0];if(l){setTimeout(function(){j(l)},0)}},"mouseup mouseleave":function(){g(this).unbind(h)}};function j(k){if(k.click){k.click()}else{var l=document.createEvent("MouseEvents");l.initMouseEvent("click",true,true,window,0,0,0,0,0,false,false,false,false,0,null);k.dispatchEvent(l)}}function d(l,k){return(l===k)||g.contains(k,l)}function b(m){var n=m.getAttribute("aria-owns"),k=m.getAttribute("aria-haspopup"),l=document.getElementById(n);if(l){return l}else{if(!n){throw new Error("Dropdown 2 trigger required attribute not set: aria-owns")}if(!k){throw new Error("Dropdown 2 trigger required attribute not set: aria-haspopup")}if(!l){throw new Error("Dropdown 2 trigger aria-owns attr set to nonexistent id: "+n)}throw new Error("Dropdown 2 trigger unknown error. I don't know what you did, but there's smoke everywhere. Consult the documentation.")}}e.delegate(".aui-dropdown2-trigger",c);e.delegate(".aui-dropdown2-trigger:not(.active):not([aria-disabled=true])",a);e.delegate(".aui-dropdown2-checkbox:not(.disabled)","click",function(){var k=g(this);if(k.hasClass("checked")){k.removeClass("checked").attr("aria-checked","false");k.trigger("aui-dropdown2-item-uncheck")}else{k.addClass("checked").attr("aria-checked","true");k.trigger("aui-dropdown2-item-check")}});e.delegate(".aui-dropdown2-radio:not(.checked):not(.disabled)","click",function(){var k=g(this);var l=k.closest("ul").find(".checked");l.removeClass("checked").attr("aria-checked","false").trigger("aui-dropdown2-item-uncheck");k.addClass("checked").attr("aria-checked","true").trigger("aui-dropdown2-item-check")});e.delegate(".aui-dropdown2 a.disabled","click",function(k){k.preventDefault()})})(jQuery);
