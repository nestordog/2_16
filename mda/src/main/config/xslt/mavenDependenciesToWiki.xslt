<?xml version="1.0" encoding="UTF-8"?>
<?altova_samplexml algoTrader.xmi?>
<xsl:stylesheet version="2.0" xmlns:mvn="http://maven.apache.org/POM/4.0.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
{| class="wikitable"
! ArtifactId !! GroupId !! Version
|-
<xsl:for-each select="//mvn:dependency">| <xsl:value-of select="mvn:groupId"/> || <xsl:value-of select="mvn:artifactId"/> || <xsl:value-of select="mvn:version"/>
|-
</xsl:for-each>
|}
</xsl:template>
</xsl:stylesheet>
