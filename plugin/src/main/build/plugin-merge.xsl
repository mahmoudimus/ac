<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>
    <xsl:param name="baseDir"/>

    <xsl:template match="application">
        <xsl:comment><xsl:value-of select="@key"/> modules</xsl:comment>
<xsl:text>
</xsl:text>

        <xsl:for-each select="node()[name(.)!='permissions']">
            <xsl:choose>
                <xsl:when test="name(.)='macro-page' or name(.)='remote-macro'">
                    <xsl:copy>
                        <xsl:attribute name="application"><xsl:value-of select="../@key" /></xsl:attribute>
                        <xsl:apply-templates select="@*|node()"/>
                    </xsl:copy>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy>
                        <xsl:attribute name="application"><xsl:value-of select="../@key" /></xsl:attribute>
                        <xsl:attribute name="key"><xsl:value-of select="../@key" />-<xsl:value-of select="@key" /></xsl:attribute>
                        <xsl:if test="name(.)='described-module-type'">
                            <xsl:attribute name="type"><xsl:value-of select="@key" /></xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates select="@*[name(.)!='key']|node()"/>
                    </xsl:copy>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:for-each>
<xsl:text>
</xsl:text>
    </xsl:template>

    <xsl:template match="plugin-info/permissions">
        <xsl:copy>
            <xsl:apply-templates select="permission"/>
            <xsl:apply-templates select="document(concat('file:///',$baseDir,'/atlassian-plugin-confluence.xml'))/application/permissions/permission"/>
            <xsl:apply-templates select="document(concat('file:///',$baseDir,'/atlassian-plugin-jira.xml'))/application/permissions/permission"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="application/permissions/permission">
        <xsl:copy>
            <xsl:attribute name="application"><xsl:value-of select="../../@key" /></xsl:attribute>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="atlassian-plugin">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:apply-templates select="document(concat('file:///',$baseDir,'/atlassian-plugin-confluence.xml'))"/>
            <xsl:apply-templates select="document(concat('file:///',$baseDir,'/atlassian-plugin-jira.xml'))"/>
            <xsl:apply-templates select="document(concat('file:///',$baseDir,'/atlassian-plugin-stash.xml'))"/>
        </xsl:copy>
    </xsl:template>

    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>