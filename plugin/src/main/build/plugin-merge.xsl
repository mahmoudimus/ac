<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>
    <xsl:param name="baseDir"/>

    <xsl:template match="application">
        <xsl:comment><xsl:value-of select="@key"/> modules</xsl:comment>
<xsl:text>
</xsl:text>
        <xsl:for-each select="node()">
            <xsl:copy>
                <xsl:attribute name="application"><xsl:value-of select="../@key" /></xsl:attribute>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:for-each>
<xsl:text>
</xsl:text>
    </xsl:template>

    <xsl:template match="atlassian-plugin">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:apply-templates select="document(concat($baseDir,'/atlassian-plugin-refapp.xml'))"/>
            <xsl:apply-templates select="document(concat($baseDir,'/atlassian-plugin-confluence.xml'))"/>
            <xsl:apply-templates select="document(concat($baseDir,'/atlassian-plugin-jira.xml'))"/>
        </xsl:copy>
    </xsl:template>

    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>