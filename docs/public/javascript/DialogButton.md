<section>

<header>
    <h1>
    DialogButton
    </h1>
    
</header>

<article>
    <div class="container-overview">
    

    
        

<dd>
    
    
    <div class="class-description">
        <p>A dialog button that can be controlled with javascript</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

    
    </div>

    

    

    

    

    

    

    
        <h2 class="subsection-title">Methods</h2>
        <div class="ac-js-methods">
        <dl>
            


    <dt>
        <h3 class="name" id="bind"><code>bind</code><span class="signature">(callback)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Registers a function to be called when the button is clicked.</p>
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

            

            

            <td class="description last"><p>function to be triggered on click or programatically.</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.getButton('submit').bind(function(){
    alert('clicked!');
  });
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="disable"><code>disable</code><span class="signature">()</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Sets the button state to disabled</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.getButton('submit').disable();
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="enable"><code>enable</code><span class="signature">()</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Sets the button state to enabled</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.getButton('submit').enable();
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="isEnabled"><code>isEnabled</code><span class="signature">(callback)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Query a button for it's current state.</p>
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

            

            

            <td class="description last"><p>function to receive the button state.</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.getButton('submit').isEnabled(function(enabled){
    if(enabled){
      //button is enabled
    }
  });
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="toggle"><code>toggle</code><span class="signature">()</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Toggle the button state between enabled and disabled.</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.getButton('submit').toggle();
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="trigger"><code>trigger</code><span class="signature">()</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Trigger a callback bound to a button.</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.getButton('submit').bind(function(){
    alert('clicked!');
  });
  dialog.getButton('submit').trigger();
});</code></pre>


    
</dd>

        </dl>
        </div>
    

    

    
</article>

</section>