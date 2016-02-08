_AP.util = {
  escapeSelector: function(s) {
    if (!s) {
      throw new Error('No selector to escape');
    }
    return s.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^`{|}~]/g, '\\$&');
  }
};