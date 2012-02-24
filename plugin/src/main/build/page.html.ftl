[#ftl]
[#assign basePath][#list 1..depth as i]../[/#list][/#assign]
[#assign title = title?substring(0, title?length - 5)]
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="content-type" content="text/html;charset=utf-8">
  <title>${title}</title>
  <link href="http://circumflex.ru/css/docco.css"
        rel="stylesheet"
        type="text/css"
        media="screen, projection"/>
    <!-- Include jQuery (Syntax Highlighter Requirement) -->
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
    <!-- Include jQuery Syntax Highlighter -->
    <script type="text/javascript" src="http://balupton.github.com/jquery-syntaxhighlighter/scripts/jquery.syntaxhighlighter.min.js"></script>
    <!-- Initialise jQuery Syntax Highlighter -->
    <script type="text/javascript">
        $.SyntaxHighlighter.init({
            'lineNumbers': false
         });

        $(document).ready(function() {

           $.each($('.code'), function() {
               var $this = $(this);
               var code = $('pre', $this);
               var lines = code.text().split('\n');
               var compText = "";
               $.each(lines, function() {
                   var line = this;
                   var matches = /^(\s*)(.*)$/.exec(line)
                   var prefix = matches[1].length;
                   var compLine = (prefix > 2 ? Array(Math.floor(prefix/2)).join(" ") : "") + matches[2];
                   compText += compLine + "\n";
               });
               compText = compText.substring(0, compText.length - 1);
               code.text(compText);
               $('.linecount', $this).text(lines.length);
               $('.hidden-code', $this).hide();
           });
           $('.hidden-code-toggle').click(function (e) {
               var $target = $(e.target);
               $(".hidden-code", $target.parent().parent()).slideToggle("slow", function() {
                   var $this = $(this);
                   if ($this.is(":visible")) {
                       $target.text("Hide");
                   }
                   else {
                       $target.text("Show");
                   }
                   RA.resize();
               });
            });
          RA.resize();
          var topLocationHash = RA.getLocation().hash;
          if (topLocationHash) {
            $('a[name="' + topLocationHash + '"]').scrollIntoView(true);
          }
        });
    </script>
  <script src="https://remoteapps.jira.com/wiki/remoteapps/all.js" type="text/javascript"></script>
    <style type="text/css">
        pre.prettyprint {
            border: 0 !important;
        }
        div.hidden-code-line, a.hidden-code-toggle {
            color: gray;
        }
    </style>
</head>
<body>
<div id="container">
  <table cellspacing="0" cellpadding="0">
    <tbody>
    [#list sections as sec]
    [#assign secDoc = sec.doc]
    [#assign secId = "section-" + sec_index]
    [#if secDoc?starts_with("<p>#")]
        [#assign secId = secDoc?substring(4, sec.doc?index_of("\n"))]
        [#assign secDoc = "<p>" + secDoc?substring(secDoc?index_of("\n") + 1)]
    [/#if]
    <tr id="${secId}">
        [#assign toHide = !(secDoc?has_content) || secDoc?starts_with("<p>-")]
      <td class="docs">
        <div class="octowrap">
          <a class="octothorpe" href="#${secId}">#</a>
        </div>
          [#if !toHide]
            ${secDoc}
          [/#if]
      </td>
      <td class="code">
          [#if toHide]
            [#assign snippetTitle = ""]
            [#if secDoc?has_content]
                [#assign snippetTitle = secDoc?substring(4, secDoc?length - 4)]
            [/#if]
            <div class="hidden-code-line">
                ------------------
                <a href="javascript:void(0)" class="hidden-code-toggle">View</a> ${snippetTitle} code (<span class="linecount"></span> lines)
                ------------------
            </div>
              <pre class="language-java hidden-code">${sec.code?html}</pre>
          [#else]
              <pre class="language-java">${sec.code?html}</pre>
          [/#if]
      </td>
    </tr>
    [/#list]
  </table>
</div>
<script type="text/javascript">
  RA.init();
</script>
</body>
</html>