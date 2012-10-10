var context = appContext.getBean("renderContext");

// @todo if this is reparsed on every request, we may want to logically make
// module.exports = snapshot(), since the values shouldn't change in the scope of a single
// request (at least, not once we've reached the ringo layer in the execution stack)

var properties = [];

Object.keys(context).forEach(function (k) {
  if (k.indexOf("get") === 0) {
    var p = k.charAt(3).toLowerCase() + k.slice(4);
    properties.push(p);
    exports[p] = function () { return context[k](); };
  }
});

exports.snapshot = function () {
  var json = {};
  properties.forEach(function (p) {
    json[p] = exports[p]();
  });
  return json;
};
