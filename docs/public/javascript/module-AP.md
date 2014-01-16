<section>

<header>
    <h1>
    AP
    </h1>
    
</header>

<article>
    <div class="container-overview">
    

    
        

        
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>


        
    
    </div>

    

    

    

    

    

    

    
        <h2 class="subsection-title">Methods</h2>
        <div class="ac-js-methods">
        <dl>
            


    <dt>
        <h3 class="name" id="clearMessage"><code>clearMessage</code><span class="signature">(id)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>clears a message by id in the host application</p>
    </div>
    

    
    
    
    
    
        <h5>Parameters:</h5>
        

<table class="params table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>id</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the message id</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.clearMessage('123');</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="fireEvent"><code>fireEvent</code><span class="signature">(id, props)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>fire an analytics event</p>
    </div>
    

    
    
    
    
    
        <h5>Parameters:</h5>
        

<table class="params table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>id</code></td>
            

            <td class="type">
            
            </td>

            

            

            <td class="description last"><p>the event id.  Will be prepended with the prefix &quot;p3.iframe.&quot;</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>props</code></td>
            

            <td class="type">
            
            </td>

            

            

            <td class="description last"><p>the event properties</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    
        <dt class="important tag-deprecated">This functionality is deprecated and may be removed in the future.</dt>
    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="getLocation"><code>getLocation</code><span class="signature">(callback)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>get the location of the host page</p>
    </div>
    

    
    
    
    
    
        <h5>Parameters:</h5>
        

<table class="params table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>callback</code></td>
            

            <td class="type">
            
                
<span class="param-type">function</span>


            
            </td>

            

            

            <td class="description last"><p>function (location) {...}</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.getLocation(function(location){
  alert(location); 
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="getTimeZone"><code>getTimeZone</code><span class="signature">(callback)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>get current timezone - if user is logged in then this will retrieve user's timezone
the default (application/server) timezone will be used for unauthorized user</p>
    </div>
    

    
    
    
    
    
        <h5>Parameters:</h5>
        

<table class="params table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>callback</code></td>
            

            <td class="type">
            
                
<span class="param-type">function</span>


            
            </td>

            

            

            <td class="description last"><p>function (user) {...}</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.getTimeZone(function(timezone){
  console.log(timezone);
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="getUser"><code>getUser</code><span class="signature">(callback)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>get a user object containing the user's id and full name</p>
    </div>
    

    
    
    
    
    
        <h5>Parameters:</h5>
        

<table class="params table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>callback</code></td>
            

            <td class="type">
            
                
<span class="param-type">function</span>


            
            </td>

            

            

            <td class="description last"><p>function (user) {...}</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.getUser(function(user){ 
  console.log(user);
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="resize"><code>resize</code><span class="signature">(width, height)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>resize this iframe</p>
    </div>
    

    
    
    
    
    
        <h5>Parameters:</h5>
        

<table class="params table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>width</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the desired width</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>height</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the desired height</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="showMessage"><code>showMessage</code><span class="signature">(id, title, body)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>shows a message with body and title by id in the host application</p>
    </div>
    

    
    
    
    
    
        <h5>Parameters:</h5>
        

<table class="params table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>id</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the message id</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>title</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the message title</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>body</code></td>
            

            <td class="type">
            
                
<span class="param-type">string</span>


            
            </td>

            

            

            <td class="description last"><p>the message body</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.showMessage('123', 'Hello world title', 'Hello world body');</code></pre>


    
</dd>

        </dl>
        </div>
    

    

    
</article>

</section>