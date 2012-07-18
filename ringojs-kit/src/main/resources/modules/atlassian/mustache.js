exports.render = function(templatePath, vars) {
  return appContext.getBean("httpUtils").render(templatePath, vars);
};