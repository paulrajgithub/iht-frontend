<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan">

    <xsl:include href="pdf/templates/application/return/assets.xsl"/>
    <xsl:include href="pdf/templates/application/return/debts.xsl"/>
    <xsl:include href="pdf/templates/application/return/exemptions.xsl"/>
    <xsl:include href="pdf/templates/application/return/gifts.xsl"/>
    <xsl:include href="pdf/templates/application/return/tnrb.xsl"/>
    <!-- <xsl:include href="pdf/templates/iht-component-templates.xsl"/>-->

    <xsl:template match="IHTReturn">
        <xsl:comment>Free Estate section starts</xsl:comment>
        <xsl:call-template name="assets">
            <xsl:with-param name="value" select="freeEstate"></xsl:with-param>
        </xsl:call-template>
        <xsl:comment>Free Estate section ends</xsl:comment>

        <xsl:comment>Debts section starts</xsl:comment>
        <xsl:call-template name="debts">
            <xsl:with-param name="value" select="freeEstate"></xsl:with-param>
        </xsl:call-template>
        <xsl:comment>Debts section ends</xsl:comment>

        <xsl:comment>Exemptions section starts</xsl:comment>
        <xsl:call-template name="exemptions">
            <xsl:with-param name="value" select="freeEstate"></xsl:with-param>
        </xsl:call-template>
        <xsl:comment>Exemptions section ends</xsl:comment>

        <xsl:comment>Gifts section starts</xsl:comment>
        <xsl:call-template name="gifts">
            <xsl:with-param name="value" select="gifts"></xsl:with-param>
        </xsl:call-template>
        <xsl:comment>Gifts section ends</xsl:comment>

        <xsl:comment>TNRB section starts</xsl:comment>
        <xsl:call-template name="tnrb">
            <xsl:with-param name="value" select="deceased"></xsl:with-param>
        </xsl:call-template>
        <xsl:comment>TNRB section ends</xsl:comment>
    </xsl:template>
</xsl:stylesheet>