[#ftl]
<remote-app key="remoteapps-docco-app" name="Remote App plugin docco app" version="1" icon-url="http://atlassian.com/favicon.ico"
            display-url="http://linode.twdata.org/remoteapps-docco-app">
  <vendor name="Atlassian" url="http://atlassian.com" />
  <description>
    Displays Docco-generated code from the Remote Apps plugin
  </description>
[#list dirs as dir]
      [#list index[dir] as file]
        <page-macro key="doc-${file?replace(".java","")}" name="${file?replace(".java","")}" url="/${dir}/${file}.html"/>
      [/#list]
[/#list]
</remote-app>