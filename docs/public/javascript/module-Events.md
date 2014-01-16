<section>

<header>
    <h1>
    Events
    </h1>
    
</header>

<article>
    <div class="container-overview">
    

    
        
            <div class="class-description"><p>The Events module provides a mechanism for emitting and receiving events.</p>
<p><h3>Basic example</h3></p>
<pre><code>//The following will create an alert message every time the event `customEvent` is triggered.
AP.require('events', function(events){
  events.on('customEvent', function(){
      alert('event fired');
  });
  events.emit('customEvent');
});</code></pre></div>
        

        
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>


        
    
    </div>

    

    

    

    

    

    

    
        <h2 class="subsection-title">Methods</h2>
        <div class="ac-js-methods">
        <dl>
            


    <dt>
        <h3 class="name" id="emit"><code>emit</code><span class="signature">(name, args)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Emits an event on this bus, firing listeners by name as well as all 'any' listeners. Arguments following the
name parameter are captured and passed to listeners.</p>
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
            
                <td class="name"><code>name</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>The name of event to emit</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>args</code></td>
            

            <td class="type">
            
                
<span class="param-type">String[]</span>


            
            </td>

            

            

            <td class="description last"><p>0 or more additional data arguments to deliver with the event</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="off"><code>off</code><span class="signature">(name, listener)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Unsubscribes a callback to an event name.</p>
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
            
                <td class="name"><code>name</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>The event name to unsubscribe the listener from</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>listener</code></td>
            

            <td class="type">
            
                
<span class="param-type">function</span>


            
            </td>

            

            

            <td class="description last"><p>The listener callback to unsubscribe from the event name</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="offAll"><code>offAll</code><span class="signature">(<span class="optional">name</span>)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Unsubscribes all callbacks from an event name, or unsubscribes all event-name-specific listeners
if no name if given.</p>
    </div>
    

    
    
    
    
    
        <h5>Parameters:</h5>
        

<table class="params table table-striped aui">
    <thead>
	<tr>
		
		<th>Name</th>
		

		<th>Type</th>

		
		<th>Argument</th>
		

		

		<th class="last">Description</th>
	</tr>
	</thead>

	<tbody>
	

        <tr>
            
                <td class="name"><code>name</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            
                <td class="attributes">
                
                    &lt;optional><br>
                

                

                
                </td>
            

            

            <td class="description last"><p>The event name to unsubscribe all listeners from</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="offAny"><code>offAny</code><span class="signature">(listener)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Unsubscribes a callback from the set of 'any' event listeners.</p>
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
            
                <td class="name"><code>listener</code></td>
            

            <td class="type">
            
                
<span class="param-type">function</span>


            
            </td>

            

            

            <td class="description last"><p>A listener callback to unsubscribe from any event name</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="on"><code>on</code><span class="signature">(name, listener)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Subscribes a callback to an event name.</p>
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
            
                <td class="name"><code>name</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>The event name to subscribe the listener to</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>listener</code></td>
            

            <td class="type">
            
                
<span class="param-type">function</span>


            
            </td>

            

            

            <td class="description last"><p>A listener callback to subscribe to the event name</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="onAny"><code>onAny</code><span class="signature">(listener)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Subscribes a callback to all events, regardless of name.</p>
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
            
                <td class="name"><code>listener</code></td>
            

            <td class="type">
            
                
<span class="param-type">function</span>


            
            </td>

            

            

            <td class="description last"><p>A listener callback to subscribe for any event name</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="once"><code>once</code><span class="signature">(name, listener)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Subscribes a callback to an event name, removing it once fired.</p>
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
            
                <td class="name"><code>name</code></td>
            

            <td class="type">
            
                
<span class="param-type">String</span>


            
            </td>

            

            

            <td class="description last"><p>The event name to subscribe the listener to</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>listener</code></td>
            

            <td class="type">
            
                
<span class="param-type">function</span>


            
            </td>

            

            

            <td class="description last"><p>A listener callback to subscribe to the event name</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        </dl>
        </div>
    

    

    
</article>

</section>