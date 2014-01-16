<section>

<header>
    <h1>
    RequestProperties
    </h1>
    
</header>

<article>
    <div class="container-overview">
    

    
        

<dd>
    
    
    <div class="class-description">
        <p>An object containing the options of a Request</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

        <h5 class="subsection-title">Properties:</h5>

        <dl>

<table class="props table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>url</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the url to request from the host application, relative to the host's context path</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>type</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the HTTP method name; defaults to 'GET'</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>data</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the string entity body of the request; required if type is 'POST' or 'PUT'</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>contentType</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>the content-type string value of the entity body, above; required when data is supplied</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>headers</code></td>
            

            <td class="type">
            
                
<span class="param-type">Object</span>


            
            </td>

            

            

            <td class="description last"><p>an object containing headers to set; supported headers are: Accept</p></td>
        </tr>

	
	</tbody>
</table>
</dl>

    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

    
    </div>

    

    

    

    

    

    

    

    

    
</article>

</section>