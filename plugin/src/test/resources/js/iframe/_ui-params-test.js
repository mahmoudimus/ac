(function(){
    define(['iframe/_ui-params'], function() {

        _AP.require(["_ui-params", "_base64"], function(uiParams, base64) {

            module('Ui Params', {
                setup: function(){
                    this.paramObj = { foo: 'bar', a: "b" };
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

            test("decoding an invalid set of options returns a blank object", function(){
                var invalid = this.encodedParamObj + 'a!bc';
                deepEqual(uiParams.decode(invalid), {});
            });

            test("decoding an encoded object returns the original", function(){
                var encoded = uiParams.encode(this.paramObj);
                var decoded = uiParams.decode(encoded);
                deepEqual(this.paramObj, decoded);
            });


            test('fromUrl returns the param object from a url', function() {
                var remoteUrl = 'http://www.example.com?a=jira:12345&ui-params=' + this.encodedParamObj;
                deepEqual(uiParams.fromUrl(remoteUrl), this.paramObj);
            });

        });

    });

})();
