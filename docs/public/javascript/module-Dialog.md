<section>

<header>
    <h1>
    Dialog
    </h1>
    
</header>

<article>
    <div class="container-overview">
    

    
        
            <div class="class-description"><p>The Dialog module provides a mechanism for launching modal dialogs from within an add-on's iframe.
A modal dialog displays information without requiring the user to leave the current page.
The dialog is opened over the entire window, rather than within the iframe itself.</p>
<p>For more information, read about the Atlassian User Interface <a href="https://docs.atlassian.com/aui/latest/docs/dialog.html">dialog component</a>.</p></div>
        

        
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>


        
    
    </div>

    

    

    

    

    

    

    
        <h2 class="subsection-title">Methods</h2>
        <div class="ac-js-methods">
        <dl>
            


    <dt>
        <h3 class="name" id="close"><code>close</code><span class="signature">(data)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Closes the currently open dialog. Optionally pass data to listeners of the <code>dialog.close</code> event.
This will only close a dialog that has been opened by your add-on.
You can register for close events using the <code>dialog.close</code> event and the <a href="module-Event.html">events module</a></p>
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

            

            

            <td class="description last"><p>An object to be emitted on dialog close.</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.close({foo: 'bar'});
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="create"><code>create</code><span class="signature">()</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Creates a dialog for a module key</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.create('mydialog');
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="getButton"><code>getButton</code><span class="signature">()</span><span class="type-signature"> &rarr; {<a href="DialogButton.html">DialogButton</a>}</span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Returns the button that was requested (either cancel or submit)</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    <h5>Returns:</h5>
    
            


		
<span class="param-type"><a href="DialogButton.html">DialogButton</a></span>




        
    
    
        <h5>Example</h5>
        
    <pre><code>AP.require('dialog', function(dialog){
  dialog.getButton('submit');
});</code></pre>


    
</dd>

        
            


    <dt>
        <h3 class="name" id="onDialogMessage"><code>onDialogMessage</code><span class="signature">(String, Function)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>register callbacks responding to messages from the host dialog, such as &quot;submit&quot; or &quot;cancel&quot;</p>
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
            
                <td class="name"><code>String</code></td>
            

            <td class="type">
            
            </td>

            

            

            <td class="description last"><p>button either &quot;cancel&quot; or &quot;submit&quot;</p></td>
        </tr>

	

        <tr>
            
                <td class="name"><code>Function</code></td>
            

            <td class="type">
            
            </td>

            

            

            <td class="description last"><p>callback function</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    
        <dt class="important tag-deprecated">This functionality is deprecated and may be removed in the future.</dt>
    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        </dl>
        </div>
    

    

    
</article>

</section>