//map AP.env.getUser to AP.user.getUser for compatibility.
if(AP._hostModules.user && AP.env) {
  AP._hostModules.env.getUser = AP._hostModules.user.getUser;  
  AP.env.getUser = AP._hostModules.user.getUser;
}