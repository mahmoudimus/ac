# Pages
<!-- ## Seamless iframes -->

The content for a page module is injected into the Atlassian application in the form of a "seamless" iframe. Seamless iframes are regular HTML iframes but with the following characteristics:

 * Their size is based on the page height and width inside the iframe (i.e., no scrollbars)
 * They are dynamically resized based on the inner content or relative browser window sizing
 * They appear without borders, making them look like a non-iframed fragment of the page
 * For general-pages, you can also opt to size your iframe to take up all of the browser window's space (instead of resizing to its internal content). To do this, add the data-option attribute "sizeToParent:true" in the script tag for all.js. For example, using ACE:

 ```
 <script src="{{hostScriptUrl}}" type="text/javascript" data-options="sizeToParent:true"></script>
 ```

As implied here, for most page content modules, you do not need to be concerned with iframe sizing. It's all handled for you. However, an exception exists for inline macros.

An inline macro is a type of macro that generates content within the text flow of a paragraph or other text element in which the macro appears, such as a status lozenge.

To implement an inline macro, follow these general guidelines:

1. In your macro-page declaration in the add-on descriptor, set the output-type attribute to inline. (Alternatively, if this value is set to block, the macro content will appear on a new line in the page output.)
2. If the output content should occupy a certain width and height, set those values as the width and height attributes for the element.
3. To prevent the macro output from being automatically resized, set the `data-options` attribute in the script tag for all.js to "`resize:false`". This turns off automatic resizing of the iframe.
4. If the size of the macro output content size is dynamic, call `AP.resize(w,h)` immediately after the DOM of your iframe is loaded.

