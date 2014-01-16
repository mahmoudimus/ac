<section>

<header>
    <h1>
    WorkflowConfiguration
    </h1>
    
</header>

<article>
    <div class="container-overview">
    

    
        

<dd>
    
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

    
    </div>

    

    

    

    

    

    

    
        <h2 class="subsection-title">Methods</h2>
        <div class="ac-js-methods">
        <dl>
            


    <dt>
        <h3 class="name" id="getUuid"><code>getUuid</code><span class="signature">()</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Get the workflow unique id</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="onSave"><code>onSave</code><span class="signature">(listener)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Attach a callback function to run when a workflow is saved</p>
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

            

            

            <td class="description last"><p>called on save.</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="onSaveValidation"><code>onSaveValidation</code><span class="signature">(listener)</span><span class="type-signature"></span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Validate a workflow configuration before saving</p>
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

            

            

            <td class="description last"><p>called on validation. Return false to indicate that validation has not passed and the workflow cannot be saved.</p></td>
        </tr>

	
	</tbody>
</table>

    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    
    
</dd>

        
            


    <dt>
        <h3 class="name" id="trigger"><code>trigger</code><span class="signature">()</span><span class="type-signature"> &rarr; {<a href="WorkflowConfigurationTriggerResponse.html">WorkflowConfigurationTriggerResponse</a>}</span></h3>
        
    </dt>

<dd>
    
    
    <div class="class-description">
        <p>Save a workflow configuration if valid.</p>
    </div>
    

    
    
    
    
    
    
    
<dl class="details">
    

    

    

    

    

    

    

    

    

    

    

    
</dl>

    
    

    

    
    
    
    
    
    <h5>Returns:</h5>
    
            
<div class="param-desc">
    <p>An object Containing <code>{valid, uuid, value}</code> properties.valid (the result of the validation listener), uuid and value (result of onSave listener) properties.</p>
</div>



		
<span class="param-type"><a href="WorkflowConfigurationTriggerResponse.html">WorkflowConfigurationTriggerResponse</a></span>




        
    
    
</dd>

        </dl>
        </div>
    

    

    
</article>

</section>