<section>

<header>
    <h1>
    confluence
    </h1>
    
</header>

<article>
    <div class="container-overview">
    

    
        
            <div class="class-description"><p>Interact with the confluence macro editor.</p></div>
        

        
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>


        
    
    </div>

    

    

    

    

    

    

    
        <h2 class="subsection-title">Methods</h2>
        <div class="ac-js-methods">
        <dl>
            


    <dt>
        <h3 class="name" id="closeMacroEditor"><code>closeMacroEditor</code><span class="signature">()</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Closes the macro editor, if it is open.
This call does not save any modified parameters to the macro, and saveMacro should be called first if necessary.</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('confluence', function(confluence){
  confluence.closeMacroEditor();
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="saveMacro"><code>saveMacro</code><span class="signature">(data)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Save a macro with data that can be accessed when viewing the confluence page.</p>
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
            
                <td class="name"><code>data</code></td>
            

            <td class="type">
            
                
<span class="param-type">Object</span>


            
            </td>

            

            

            <td class="description last"><p>to be saved with the macro.</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('confluence', function(confluence){
  confluence.saveMacro({foo: 'bar'});
});</code></pre>


    
</dd>

        </dl>
        </div>
    

    

    
</article>

</section>