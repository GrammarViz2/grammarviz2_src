<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" indent="yes"/>

<xsl:template match="/">
  <xmldata>
    <xsl:apply-templates select="report/data/all/coverage"/>  
  </xmldata>
</xsl:template>

<xsl:template match="coverage">
  <xsl:value-of select="@type"/> 
  <xsl:text>  </xsl:text>  
  <xsl:value-of select="@value"/> 
  <xsl:text>&#10;</xsl:text>
</xsl:template>

</xsl:stylesheet>
