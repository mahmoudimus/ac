##Tutorial: Build a Connect add-on for your JIRA projects

(intro draft) In this tutorial, you'll build a static Connect add-on that 
displays your JIRA projects. We affectionately call this add-on 'Geiger'. 
Geiger is a node.js app that interfaces with JIRA. To do for this section:

* Explain the concepts we'll go over  
* Explan how to access the add-on from inside the JIRA interface  
* Show screenshot and TOC  

Here's what's here: (placeholder for TOC)  

* [Configure your development environment](#environment)  
* [Trim the plugin skeleton](#trimming)  
* [Add a link in the header for stats](#stats-header)  

## <a name="environment"></a> Configure your development environment  

In this step, you'll confirm you have node.js installed, and install the 
[Atlassian Connect Express (ACE)](https://bitbucket.org/atlassian/atlassian-connect-express/) framework. The ACE framework is a toolkit for creating Connect add-ons using node.js.

You'll use ACE to create a new node.js project called `tut`.

1. Install [node.js](http://www.nodejs.org/).
	If you use [Homebrew](http://brew.sh/), you can use the following command:
	<pre><code data-lang="text">$ brew install node</code></pre>
	Otherwise, you can [download and install node directly](http://nodejs.org/download/).
1. Install the [ACE framework](https://bitbucket.org/atlassian/atlassian-connect-express/).
	<pre><code data-lang="text">$ npm install -g atlas-connect</code></pre>
1. Create a new ACE project called `tut`.
	<pre><code data-lang="text">$ atlas-connect new tut</code></pre>
1. Change to your new `tut` directory.
	<pre><code data-lang="text">$ cd tut</code></pre>
1. Install node.js dependencies for your project.  
	<pre><code data-lang="text">$ npm install</code></pre>
1. Ensure you have the [Atlassian SDK installed](https://developer.atlassian.com/display/DOCS/Downloads).  
    You'll need at least SDK version 4.2.20. If you run the <tt>atlas-version</tt> command, 
    you should see something similar to this:  

    <tt>
        ATLAS Version:    4.2.20  
        ATLAS Home:       /usr/share/atlassian-plugin-sdk-4.2.20  
        ATLAS Scripts:    /usr/share/atlassian-plugin-sdk-4.2.20/bin  
        ATLAS Maven Home: /usr/share/atlassian-plugin-sdk-4.2.20/apache-maven  
    </tt>
1. Start JIRA in OnDemand mode with the following command:
	<pre><code data-lang="text">atlas-run-standalone --product jira --version 6.3-OD-07-010 --bundled-plugins com.atlassian.plugins:atlassian-connect-plugin:1.0.2,com.atlassian.jwt:jwt-plugin:1.0.0,com.atlassian.bundles:json-schema-validator-atlassian-bundle:1.0-m0  --jvmargs -Datlassian.upm.on.demand=true</code></pre>

    __Note:__ If you're not using the command above, ensure all components in the 
    `--bundled-plugins` argument are present in your JIRA instances. These component 
    versions will change as Connect development continues.  
    
    You'll see a lot of output. When finished, your terminal notifies you that the build 
    was successful:  
    <tt>[INFO] [talledLocalContainer] Tomcat 7.x started on port [2990]  
        [INFO] jira started successfully in 217s at http://localhost:2990/jira  
        [INFO] Type Ctrl-D to shutdown gracefully  
        [INFO] Type Ctrl-C to exit
    </tt>  
1. Log in with `admin/admin`. 

## <a name="trimming"></a>Trim the add-on skeleton 

You now have the basic architecture for your plugin. If you open your new `tut` project, 
you'll see essentials like the [`atlassian-connect.json` descriptor](../modules/) in the 
project root. You'll also see an `app.js` file. (flesh out)  

In this step, you'll prune some of the stub code, and install your add-on in JIRA.

1. Open the [`atlassian-connect.json` descriptor](../modules/) file.
1. Remove the following section:  
	````
	 // Confluence - Add a Hello World menu item to the navigation bar
            {
                "key": "hello-world-page-confluence",
                "location": "system.header/left",
                "name": {
                    "value": "Hello World"
                },
                "url": "/hello-world",
                "conditions": [{
                    "condition": "user_is_logged_in"
            }]
         }]
    ````
1. From your project root, run the `app.js` file:
	<pre><code data-lang="text">$ node app.js</code></pre> 
	Your add-on is automatically registered in JIRA for you. 
1. Refresh JIRA.  
	You'll see the Hello World label in the header: 
	<img src="../assets/images/geiger-1-1.png" width="80%" style="border:1px solid #999;margin-top:10px;" />
1. Click __Hello World__ in the header.  
	Your page should look like this: 
	<img src="../assets/images/geiger-1-2.png" width="80%" style="border:1px solid #999;margin-top:10px;" />


## <a name="stats-header"></a>Add a navigation link to the Stats page  

You have your add-on running and installed in JIRA. Now, you'll add a navigation 
link in the header. You'll also do blah blah blah.  

Explain `location`, `name` & `value`, and how it works with the `condition`.

1. Add a 'Stats' label to the header of your instance.  
	This code should be placed inside the [`generalPages` module](../modules/jira/general-page.html).
	````
	{
	    "key": "stats",
	    "location": "system.top.navigation.bar",
	    "name": {
	        "value": "Stats"
	    },
	    "url": "/stats",
	    "conditions": [{ 
	        "condition": "user_is_logged_in"
	    }]
	}
	````
    
1. Also include the READ scope somewhere in the root object

````
"scopes": [
    "read"
]
````
    
1. We need to add the `/stats` route to our app, open up `routes/index.js` and 
   add after the `/hello-world` route is registered. 

````
app.get('/stats', addon.authenticate(), function(req, res) {
    res.render('stats', { title: "The Stats" });
});
````
    
1. To support the charting, we need to add one line to the `views/layout.hbs`
   file, right after the 
   `<script src="{{hostScriptUrl}}" type="text/javascript"></script>` 
   line.
   
````
<script src="http://d3js.org/d3.v3.min.js" charset="utf-8"></script>
````

1. Then create a `views/stats.hbs` with

````
{{!< layout}}
<header class="aui-page-header">
    <div class="aui-page-header-inner">
        <div class="aui-page-header-main intro-header">
            <h1>{{title}}</h1>
        </div>
    </div>
</header>

<div class="aui-page-panel main-panel">
    <div class="aui-page-panel-inner">
        <section class="aui-page-panel-item">
            <div class="aui-group">
                <div class="aui-item">
                    <div class="projects">
                    </div> 
                </div>
            </div>
        </section>
    </div>
</div>
<script type="text/javascript">
    $(function() {
        AG.initInstanceStats();
    });
</script>
````
  
1. Modify 'public/js/addon.js' with the following. This leverages
   d3.js to build data driven tables. We'll be using d3.js later for charting.


````
(function() {
    function getQueryParams(qs) {
        qs = qs.split("+").join(" ");

        var params = {}, tokens,
        re = /[?&]?([^=]+)=([^&]*)/g;

        while (tokens = re.exec(qs)) {
            params[decodeURIComponent(tokens[1])] = 
                decodeURIComponent(tokens[2]);
        }

        return params;
    }

    /* add-on script */
    window.AG = {};

    window.AG.apiCall = function(uri, callback) {
        AP.require('request', function(request) {
            request({
                url: uri,
                success: function(response) {
                    callback(response);
                },
                error: function(response) {
                    console.log("Error loading API (" + uri + ")");
                    console.log(arguments);
                },
                contentType: "application/json"
            });
        });
    };

    window.AG.initInstanceStats = function() {
        var params = getQueryParams(document.location.search);
        var baseUrl = params.xdm_e + params.cp + "/browse/";

        AG.apiCall('/rest/api/2/project', function(response) {
            // convert the string response to JSON
            response = JSON.parse(response);

            var d = d3.select(".projects")

            var projTable = d.append('table')
                .classed({'project': true, 'aui': true});

            var projHeadRow = projTable.append("thead").append("tr");
            projHeadRow.append("th");
            projHeadRow.append("th").text("Key");
            projHeadRow.append("th").text("Name");

            var projBody = projTable.append("tbody");

            var row = projBody.selectAll("tr")
                .data(response)
                .enter()
                .append("tr");

            row.append("td").append('span')
                .classed({'aui-avatar': true, 'aui-avatar-xsmall': true})
                .append('span')
                .classed({'aui-avatar-inner': true})
                .append('img')
                .attr('src', function(item) { return item.avatarUrls["16x16"] });

            row.append("td").append('span')
                .classed({'project-key': true, 'aui-label': true})
                .text(function(item) { return item.key; });

            row.append("td").append('span')
                .classed({'project-name': true})
                .append("a")
                .attr('href', function(item) { return baseUrl + item.key; })
                .attr('target', "_top")
                .text(function(item) { return item.name; });
        });
    };
})();
````

1. CTRL+C the node app and restart with `node app.js`
1. Create a couple projects in JIRA




