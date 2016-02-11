/**
* confluence and jira both break amd (by removing define.amd)
*/

connectHost.defineModule("cookie", connectHostCookie);
connectHost.defineModule("history", connectHostHistory);
// connectHost.defineModule("request", connectHostCookie);

connectHost.onIframeEstablished(function(data){
  data.$el.closest(".ap-content").addClass('iframe-init');
});