$(function() {

    /**
     * Renders the markdown and inserts it into article.
     *
     * Usually when creating an include element in the Markdown, it is surrounded by a paragraph tag.
     * In this case we replace the parent paragraph with the rendered markdown.
     *
     * @param $includeElement The element within which to render the include.
     * @param markdown The markdown source
     */
    function insertMarkup($includeElement, markdown) {
        var $parentParagraph = $($includeElement.parent("p"));
        var markup = marked(markdown);
        var $wrap = $("<span>").append(markup);

        if ($parentParagraph.children().length === 1) {
            $parentParagraph.replaceWith($wrap);
        } else {
            $includeElement.replaceWith($wrap);
        }

        $wrap.find('pre code').each(function(){
            cmIfyPreCodeBlocks(this);
        });

        startupCommands();
    }

    /**
     * Fetches a markdown file and renders a markdown include.
     *
     * The markdown files need to have an .html suffix to prevent Harp from rendering it within the site layout.
     *
     * @param i
     * @param include
     */
    function fetchParseAndRenderInclude(i, include) {
        var $element = $(include);
        $.get($element.data("include"))
            .done(function(source) {
                insertMarkup($element, source);
            })
            .error(function() {
                AJS.messages.error($element, {
                    body: "An error occurred when fetching this include.",
                    closeable: false
                });
            });
    }


    $("[data-include]").each(fetchParseAndRenderInclude);
});
