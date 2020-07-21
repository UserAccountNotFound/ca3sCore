<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:dbcl="http://www.liquibase.org/xml/ns/dbchangelog"
	xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext">

	<xsl:param name="LiquibaseTarget">http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd</xsl:param>

	<xsl:template match="@xsi:schemaLocation">
		<xsl:attribute name="xsi:schemaLocation" select="$LiquibaseTarget" />
	</xsl:template>

	
	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>


</xsl:stylesheet>