(function(){
    define(['iframe/_ui-params'], function() {

        _AP.require(["_ui-params", "_base64"], function(uiParams, base64) {

            module('Ui Params', {
                setup: function(){
                    this.paramObj = { foo: 'bar' };
                    this.encodedParamObj = base64.encode(JSON.stringify(this.paramObj));
                }
            });

            test('when passed an object. Encode returns a base64 json string', function () {
                equal(uiParams.encode(this.paramObj), this.encodedParamObj);
            });

            test('decode returns an object when presented with a base64 encoded json blob', function () {
                var decoded = uiParams.decode(this.encodedParamObj);
                deepEqual(decoded, this.paramObj);
            });

            test('fromUrl returns the param object from a url', function() {
                var remoteUrl = 'http://www.example.com?a=jira:12345&ui-params=' + this.encodedParamObj;
                deepEqual(uiParams.fromUrl(remoteUrl), this.paramObj);
            });

        });

    });

})();
