
var descriptor;

try {
    var f = window.__html__['src/test/resources/descriptor/descriptor-validation-results.json'];
    console.log(f);
    descriptor = JSON.parse(f);
} catch (e) {
    console.log(e);
}

module("Descriptor Validation", {
});

test("Descriptor and results were loaded", function () {
    ok(descriptor && descriptor.results && descriptor.results.length, "Descriptor was loaded and has results");
});

if (descriptor) {
    var results = descriptor.results;
    for (var i in results) {
        if (!results.hasOwnProperty(i)) {
            continue;
        }
        var result = results[i];
        (function (r) {
            test("Add-on " + r["addon"] + " has valid descriptor", function () {
                equal(r.errors.length, 0, "Validation errors found: " + JSON.stringify(r.errors));
            });
        })(result);
    }
}
