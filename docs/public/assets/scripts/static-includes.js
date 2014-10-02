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
        var $replacedElement = ($parentParagraph.children().length === 1) ? $parentParagraph : $includeElement;
        var $newElement = $($("<span>").append(markup));

        $replacedElement.replaceWith($newElement);

        renderCode($newElement);
    }

    /**
     * Fetches a markdown file and renders a markdown include.
     *
     * The mardown files need to have an .html suffix to prevent Harp from rendering it within the site layout.
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
                $element.text("An error occurred when fetching this include.")
            });
    }


    $("[data-include]").each(fetchParseAndRenderInclude);
});