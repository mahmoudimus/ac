var utils = appContext.getBean("httpUtils");

exports.render = function(templatePath, vars) {
  return utils.render(templatePath, vars);
};
