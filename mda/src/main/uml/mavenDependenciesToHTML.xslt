<?xml version="1.0" encoding="UTF-8"?>
<?altova_samplexml algoTrader.xmi?>
<xsl:stylesheet version="2.0" xmlns:mvn="http://maven.apache.org/POM/4.0.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <body>
                <table border="1">
                    <tr>
                        <th>GroupId</th>
                        <th>ArtifactId</th>
                        <th>Version</th>
                    </tr>
                    <xsl:for-each select="//mvn:dependency">
                        <tr>
                            <td>
                                <xsl:value-of select="mvn:groupId"/>
                            </td>
                            <td>
                                <xsl:value-of select="mvn:artifactId"/>
                            </td>
                            <td>
                                <xsl:value-of select="mvn:version"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
