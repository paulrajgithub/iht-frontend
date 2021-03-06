<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:common="http://exslt.org/common"
                xmlns:xalan="http://xml.apache.org" exclude-result-prefixes="common xalan"
                xmlns:i18n="java:iht.utils.pdf.MessagesTranslator"
                xmlns:formatter="java:iht.utils.pdf.PdfFormatter">

    <xsl:param name="translator"/>
    <xsl:param name="pdfFormatter"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template name="applicant-details">
        <xsl:param name="value"/>
        <fo:block font-family="OpenSans-Bold" font-size="16" font-weight="bold" page-break-before="always">
            <xsl:value-of select="i18n:getMessagesText($translator, 'page.iht.registration.registrationSummary.applicantTable.title')"/>
        </fo:block>

        <fo:block font-family="OpenSans" font-size="12pt" font-weight="normal" space-before="0.5cm">
            <fo:block>
                <fo:table space-before="0.5cm">
                    <fo:table-column column-number="1" column-width="22%"/>
                    <fo:table-column column-number="2" column-width="78%"/>
                    <fo:table-body font-size="12pt">
                        <xsl:call-template name="table-row-short-vpad-border-top-black">
                            <xsl:with-param name="label"
                                            select="i18n:getMessagesText($translator, 'iht.firstName')"/>
                            <xsl:with-param name="value" select="applicantDetails/firstName"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row-short-vpad">
                            <xsl:with-param name="label"
                                            select="i18n:getMessagesText($translator, 'iht.lastName')"/>
                            <xsl:with-param name="value" select="applicantDetails/lastName"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row-short-vpad">
                            <xsl:with-param name="label"
                                            select="i18n:getMessagesText($translator, 'iht.dateofbirth')"/>
                            <xsl:with-param name="value"
                                            select="formatter:getDateForDisplay($pdfFormatter,applicantDetails/dateOfBirth)"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row-short-vpad">
                            <xsl:with-param name="label"
                                            select="i18n:getMessagesText($translator, 'iht.nationalInsuranceNo')"/>
                            <xsl:with-param name="value" select="applicantDetails/nino"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row-uk-address">
                            <xsl:with-param name="label"
                                            select="i18n:getMessagesText($translator, 'iht.registration.contactAddress')"/>
                            <xsl:with-param name="value" select="applicantDetails/ukAddress"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row-short-vpad">
                            <xsl:with-param name="label"
                                            select="i18n:getMessagesText($translator, 'iht.registration.checklist.phoneNo.upperCaseInitial')"/>
                            <xsl:with-param name="value" select="applicantDetails/phoneNo"/>
                        </xsl:call-template>
                        <xsl:call-template name="table-row-short-vpad">
                            <xsl:with-param name="label"
                                            select="i18n:getMessagesText($translator, 'page.iht.registration.applicant.probateLocation.title')"/>
                            <xsl:with-param name="value" select="applicantDetails/country"/>
                        </xsl:call-template>
                        <xsl:comment>Blank row to display line at end of section</xsl:comment>
                        <xsl:call-template name="table-row-blank-short-vpad-border-top-grey-thin"/>
                    </fo:table-body>
                </fo:table>
            </fo:block>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>
