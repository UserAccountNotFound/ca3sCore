package de.trustable.ca3s.core.ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import javax.security.auth.x500.X500Principal;

import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.service.util.CryptoService;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;
import io.ddavison.conductor.Browser;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.*;

// @ContextConfiguration(classes=PipelineTestConfiguration.class)

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@io.ddavison.conductor.Config(
        browser = Browser.CHROME,
        url     = "http://localhost:${local.server.port}/"
)
@ActiveProfiles("dev")
public class CSRSubmitIT extends WebTestBase{

    public static final By LOC_TXT_WEBPACK_ERROR = By.xpath("//div//h1 [text() = 'An error has occured :-(']");

    public static final By LOC_LNK_ACCOUNT_MENUE =          By.xpath("//nav//a [.//span [text() = 'Account']]");
    public static final By LOC_LNK_REQ_CERT_MENUE =         By.xpath("//nav//a [.//span [text() = 'Request certificate']]");
    public static final By LOC_LNK_REQUESTS_MENUE =         By.xpath("//nav//a [.//span [text() = 'Requests']]");
    public static final By LOC_LNK_CERTIFICATES_MENUE =     By.xpath("//nav//a [.//span [text() = 'Certificates']]");
    public static final By LOC_LNK_ACCOUNT_SIGN_IN_MENUE =  By.xpath("//nav//a [span [text() = 'Sign in']]");
    public static final By LOC_LNK_ACCOUNT_SIGN_OUT_MENUE = By.xpath("//nav//a [span [text() = 'Sign out']]");

    public static final By LOC_LNK_SIGNIN_USERNAME = By.xpath("//form//input [@name = 'username']");
    public static final By LOC_LNK_SIGNIN_PASSWORD = By.xpath("//form//input [@name = 'password']");
    public static final By LOC_BTN_SIGNIN_SUBMIT = By.xpath("//form//button [@type='submit'][text() = 'Sign in']");

    public static final By LOC_BTN_REQUEST_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Request certificate']]");

    public static final By LOC_TEXT_ACCOUNT_NAME = By.xpath("//nav//span [contains(text(), '\"user\"')]");
    public static final By LOC_TEXT_CONTENT_TYPE = By.xpath("//form//dl [dt[span [text() = 'Content type']]]/dd/span");

    public static final By LOC_TA_UPLOAD_CONTENT = By.xpath("//form//textarea [@name = 'content']");
    public static final By LOC_SEL_PIPELINE = By.xpath("//form//select [@id = 'pkcsxx-pipeline']");


    public static final By LOC_TEXT_CERT_HEADER = By.xpath("//div/h2/span [text() = 'Certificate']");
    public static final By LOC_TEXT_PKIX_LABEL = By.xpath("//div//dl [dt[span [text() = 'Certificate']]]");
    public static final By LOC_SEL_CERT_FORMAT = By.xpath("//div//select [@name = 'download-format']");
    public static final By LOC_LNK_DOWNLOAD_CERT_ANCHOR = By.xpath("//dd/div/div/div/a [@id = 'certificate-download']");

    public static final By LOC_SEL_REVOCATION_REASON = By.xpath("//form//select [@name = 'revocationReason']");

    public static final By LOC_TEXT_CSR_HEADER = By.xpath("//div/h2/span [text() = 'CSR']");

    public static final By LOC_TEXT_REQUEST_LIST = By.xpath("//div/h2/span [text() = 'Request List']");
    public static final By LOC_TEXT_CERTIFICATE_LIST = By.xpath("//div/h2/span [text() = 'Certificate List']");

    public static final By LOC_SEL_CSR_ATTRIBUTE = By.xpath("//div/select [@name = 'csrSelectionAttribute']");
    public static final By LOC_SEL_CSR_CHOICE = By.xpath("//div/select [@name = 'csrSelectionChoice']");
    public static final By LOC_INP_CSR_VALUE = By.xpath("//div/input [@name = 'csrSelectionValue']");
    public static final By LOC_SEL_CSR_VALUE_SET = By.xpath("//div/select [@name = 'csrSelectionSet']");
    public static final By LOC_INP_CSR_DATE = By.xpath("//div/input [@name = 'csrSelectionValueDate']");
    public static final By LOC_INP_CSR_BOOLEAN = By.xpath("//div/input [@name = 'csrSelectionValueBoolean']");

    public static final By LOC_SEL_CERT_ATTRIBUTE = By.xpath("//div/select [@name = 'certSelectionAttribute']");
    public static final By LOC_SEL_CERT_CHOICE = By.xpath("//div/select [@name = 'certSelectionChoice']");
    public static final By LOC_INP_CERT_VALUE = By.xpath("//div/input [@name = 'certSelectionValue']");
    public static final By LOC_SEL_CERT_VALUE_SET = By.xpath("//div/select [@name = 'certSelectionSet']");
    public static final By LOC_INP_CERT_DATE = By.xpath("//div/input [@name = 'certSelectionValueDate']");
    public static final By LOC_INP_CERT_BOOLEAN = By.xpath("//div/input [@name = 'certSelectionValueBoolean']");

    public static final By LOC_TEXT_MESSAGE_NO_IP = By.xpath("//form//dl [dt[span [text() = 'Message']]]/dd/div[ul/li [contains(text(), 'is an IP address, not allowed')]]");

    public static final By LOC_TD_CSR_ITEM_PENDING = By.xpath("//table//td [starts-with(text(), 'Pending')]");

    public static final By LOC_TA_DOWNLOAD_CERT_CONTENT = By.xpath("//dd/div/div/div/a [@id = 'certificate-download']");

    public static final By LOC_INP_PKCS12_ALIAS = By.xpath("//div/input [@name = 'p12Alias']");
    public static final By LOC_LNK_DOWNLOAD_PKCS12 = By.xpath("//dd/div/div/a [@id = 'pkcs12-download']");

    public static final By LOC_TEXT_CERT_REVOCATION_REASON = By.xpath("//div//dd/span[@name = 'revocationReason']");

    public static final By LOC_SHOW_HIDE_AUDIT = By.xpath("//button[@id='showHideAudit']");
//    public static final By LOC_TABLE_AUDIT = By.xpath("//div/table [thead/tr/th/span[text() = 'Role']] [tbody/tr]");
    public static final By LOC_TABLE_AUDIT = By.xpath("//div//div [.//tr/th/span [contains(text(), 'Role')] ] [tbody/tr]");

    public static final By LOC_TABLE_AUDIT_REVOCATION_PRESENT = By.xpath("//div/table [thead/tr/th/span [contains(text(), 'Role')] ] [tbody/tr/td [contains(text(), 'Certificate revoked')]]");

    public static final By LOC_BTN_WITHDRAW_CERTIFICATE = By.xpath("//form/div/button [@type='button'][span [text() = 'Withdraw']]");
    public static final By LOC_BTN_CONFIRM_REQUEST = By.xpath("//form/div/button [@type='button'][span [text() = 'Confirm Request']]");
    public static final By LOC_BTN_BACK = By.xpath("//form/div/button [@type='submit'][span [text() = 'Back']]");


    public static final By LOC_SEL_KEY_CREATION_CHOICE = By.xpath("//div/select [@name = 'pkcsxx-key-creation']");
    public static final By LOC_SEL_KEY_LENGTH_CHOICE = By.xpath("//div/select [@name = 'pkcsxx.upload.key-length']");

    public static final By LOC_INP_C_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.C']");
    public static final By LOC_INP_CN_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.CN']");
    public static final By LOC_INP_O_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.O']");
    public static final By LOC_INP_OU_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.OU']");
    public static final By LOC_INP_L_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.L']");
    public static final By LOC_INP_ST_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.ST']");
    public static final By LOC_INP_SAN_VALUE = By.xpath("//div/input [@name = 'pkcsxx.upload.SAN']");

    public static final By LOC_INP_SECRET_VALUE = By.xpath("//div/input [@name = 'upload-secret']");
    public static final By LOC_INP_SECRET_REPEAT_VALUE = By.xpath("//div/input [@name = 'upload-secret-repeat']");


    private static final Logger LOG = LoggerFactory.getLogger(CSRSubmitIT.class);

	private static final String USER_NAME_USER = "user";
	private static final String USER_PASSWORD_USER = "user";

	private static final String USER_NAME_RA = "ra";
	private static final String USER_PASSWORD_RA = "s3cr3t";

	private static Random rand = new Random();

	@LocalServerPort
	int serverPort; // random port chosen by spring test

	@Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

	@BeforeAll
	public static void setUpBeforeClass() {
		JCAManager.getInstance();
	}

	@BeforeEach
	void init() {

	    waitForUrl();

		ptc.getInternalWebDirectTestPipeline();
		ptc.getInternalWebRACheckTestPipeline();
        prefTC.getTestUserPreference();

		if( driver == null) {
		    super.startWebDriver();
		    driver.manage().window().setSize(new Dimension(2000,768));
		}
	}

	@Test
	public void testCSRSubmitServersideDirect() throws GeneralSecurityException, IOException, InterruptedException {

		String c = "DE";
		String cn = "reqTest" + System.currentTimeMillis();
		String o = "trustable solutions";
		String ou = "nuclear research";
		String l = "Hannover";
		String st = "Lower Saxony";
		String san = "wwww." + cn;

	    String subject = "CN=" + cn + ", O="+o+", OU="+ou+", C="+ c + ", L=" + l + ", ST=" + st;
	    X500Principal subjectPrincipal = new X500Principal(subject);

		byte[] secretBytes = new byte[6];
		rand.nextBytes(secretBytes);
		String secret = "1Aa" + Base64.getEncoder().encodeToString(secretBytes);

		signIn(USER_NAME_USER, USER_PASSWORD_USER);

		validatePresent(LOC_LNK_REQ_CERT_MENUE);
		click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE );

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);

	    validatePresent(LOC_SEL_PIPELINE);

	    selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

		validatePresent(LOC_SEL_KEY_CREATION_CHOICE);
	    selectOptionByText(LOC_SEL_KEY_CREATION_CHOICE, "Serverside key creation");

	    validatePresent(LOC_SEL_KEY_LENGTH_CHOICE);
	    selectOptionByText(LOC_SEL_KEY_LENGTH_CHOICE, "RSA_2048");

        setText(LOC_INP_SECRET_VALUE, secret);
        setText(LOC_INP_SECRET_REPEAT_VALUE, secret);

        // mismatch of secret
        setText(LOC_INP_SECRET_REPEAT_VALUE, "aa"+secret+"zz");
        Assertions.assertFalse(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button disabled");

        setText(LOC_INP_SECRET_REPEAT_VALUE, secret);
        Assertions.assertTrue(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button enabled");

        setText(LOC_INP_C_VALUE, c);

        System.out.println(" ---------------- country = " + c + ", found in input field " + getText(LOC_INP_C_VALUE) );

        setText(LOC_INP_CN_VALUE, cn);
	    setText(LOC_INP_O_VALUE, o);
	    setText(LOC_INP_OU_VALUE, ou);
	    setText(LOC_INP_L_VALUE, l);
	    setText(LOC_INP_ST_VALUE, st);
	    setText(LOC_INP_SAN_VALUE, san);

	    validatePresent(LOC_BTN_REQUEST_CERTIFICATE);

	    Assertions.assertTrue(isEnabled(LOC_BTN_REQUEST_CERTIFICATE), "Expecting request button enabled");

        click(LOC_BTN_REQUEST_CERTIFICATE);

        waitForElement(LOC_TEXT_CERT_HEADER);
	    validatePresent(LOC_TEXT_CERT_HEADER);
		validatePresent(LOC_TEXT_PKIX_LABEL);

        X509Certificate newCert = checkPEMDownload(cn, "pem");

        checkPEMDownload(cn, "pemPart");

        checkPEMDownload(cn, "pemFull");

        checkPKCS12Download(cn, secret);

        validatePresent(LOC_SEL_REVOCATION_REASON);
	    selectOptionByText(LOC_SEL_REVOCATION_REASON, "superseded");

	    click(LOC_BTN_WITHDRAW_CERTIFICATE);

        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Subject");

        validatePresent(LOC_SEL_CERT_CHOICE);

        selectOptionByText(LOC_SEL_CERT_CHOICE, "equals");

        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, cn);

        By byCertSubject = By.xpath("//table//td [contains(text(), '"+cn+"')]");
        validatePresent(byCertSubject);
        click(byCertSubject);

        validatePresent(LOC_TEXT_CERT_REVOCATION_REASON);

        validatePresent(LOC_SHOW_HIDE_AUDIT);
        click(LOC_SHOW_HIDE_AUDIT);

        // ensure a revocation item regarding revocation is present
        validatePresent(LOC_TABLE_AUDIT_REVOCATION_PRESENT);

        // search by serial no
        waitForElement(LOC_LNK_CERTIFICATES_MENUE);
        validatePresent(LOC_LNK_CERTIFICATES_MENUE);
        click(LOC_LNK_CERTIFICATES_MENUE);

        // select the certificate in the cert list
        validatePresent(LOC_SEL_CERT_ATTRIBUTE);
        selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Serial");

        validatePresent(LOC_SEL_CERT_CHOICE);

        selectOptionByText(LOC_SEL_CERT_CHOICE, "equals");

        // set the serial number, decimal
        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, newCert.getSerialNumber().toString());

        validatePresent(byCertSubject);

        // set the serial number, decimal with leading zeros
        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, "00" + newCert.getSerialNumber().toString());

        validatePresent(byCertSubject);

        // set the serial number, hex
        validatePresent(LOC_INP_CERT_VALUE);
        setText(LOC_INP_CERT_VALUE, "0x" + newCert.getSerialNumber().toString(16));

        validatePresent(byCertSubject);


    }

	@Test
	public void testCSRSubmitDirect() throws GeneralSecurityException, IOException, InterruptedException {

		signIn(USER_NAME_USER, USER_PASSWORD_USER);

		validatePresent(LOC_LNK_REQ_CERT_MENUE);
		click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE );

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);

		String cn = "reqtest" + System.currentTimeMillis();
	    String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
	    X500Principal subjectPrincipal = new X500Principal(subject);
	    String csr = buildCSRAsPEM(subjectPrincipal);
        setLongText(LOC_TA_UPLOAD_CONTENT, csr);

	    validatePresent(LOC_TEXT_CONTENT_TYPE);

	    validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
	    click(LOC_BTN_REQUEST_CERTIFICATE);


        waitForElement(LOC_TEXT_CERT_HEADER);
	    validatePresent(LOC_TEXT_CERT_HEADER);
		validatePresent(LOC_TEXT_PKIX_LABEL);


        click(LOC_SEL_CERT_FORMAT);

        checkPEMDownload(cn, "pem");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pkix");
        String certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + ".crt", certTypeName, "Expect a informing name of the link");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pemPart");
        certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + ".part.pem", certTypeName, "Expect a informing name of the link");

        selectOptionByValue(LOC_SEL_CERT_FORMAT, "pemFull");
        certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + ".full.pem", certTypeName, "Expect a informing name of the link");


		validatePresent(LOC_SEL_REVOCATION_REASON);
	    selectOptionByText(LOC_SEL_REVOCATION_REASON, "superseded");

	    click(LOC_BTN_WITHDRAW_CERTIFICATE);

		waitForElement(LOC_LNK_CERTIFICATES_MENUE);
		validatePresent(LOC_LNK_CERTIFICATES_MENUE);
		click(LOC_LNK_CERTIFICATES_MENUE);

	    // select the certificate in the cert list
	    validatePresent(LOC_SEL_CERT_ATTRIBUTE);
	    selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Subject");

	    validatePresent(LOC_SEL_CERT_CHOICE);

	    selectOptionByText(LOC_SEL_CERT_CHOICE, "like");

	    validatePresent(LOC_INP_CERT_VALUE);
	    setText(LOC_INP_CERT_VALUE, cn);

	    By byCertSubject = By.xpath("//table//td [contains(text(), '"+cn+"')]");
	    validatePresent(byCertSubject);
	    click(byCertSubject);

	    validatePresent(LOC_TEXT_CERT_REVOCATION_REASON);
	}

    private X509Certificate checkPEMDownload(String cn, String format) throws InterruptedException, GeneralSecurityException, IOException {

        selectOptionByValue(LOC_SEL_CERT_FORMAT, format);

        int expectedChainLength = 1;
        String fileEx = format;
        if( "pemPart".equals(format)){
            expectedChainLength = 2;
            fileEx = "part.pem";
        }else if( "pemFull".equals(format)){
            expectedChainLength = 3;
            fileEx = "full.pem";
        }

        String certTypeName = getText(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Assertions.assertEquals(cn + "." + fileEx, certTypeName, "Expect an informing name of the link");

        click(LOC_LNK_DOWNLOAD_CERT_ANCHOR);
        Thread.sleep(500);

        File certFile = new File(downloadDir, certTypeName);
        LOG.info("downloaded certFile: {}", certFile.getAbsolutePath());

        Assertions.assertTrue(certFile.exists());
        List<X509Certificate> certificateList = convertPemToCertificateChain(new String(Files.readAllBytes(certFile.toPath())));

        Assertions.assertEquals(expectedChainLength, certificateList.size(), "Expected chain length");

//        assertTrue( "Expecting the requested Common Name included in the subject", x509Cert.getSubjectDN().getName().contains(cn) );

        return certificateList.get(0);
    }

    private void checkPKCS12Download(String cn, String secret) throws InterruptedException, GeneralSecurityException, IOException {

        byte[] secretBytes = new byte[6];
        String alias = Base64.getEncoder().encodeToString(secretBytes).replaceAll("/", "X").replaceAll("\\+", "x");

        setText(LOC_INP_PKCS12_ALIAS, alias);

        String pkcs12FileName = getText(LOC_LNK_DOWNLOAD_PKCS12);
        Assertions.assertEquals(cn + ".p12", pkcs12FileName, "Expect an informing name of the link");

/*
        try {
            System.out.println("... waiting ...");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        click(LOC_LNK_DOWNLOAD_PKCS12);
        Thread.sleep(500);

        File pkcs12File = new File(downloadDir, pkcs12FileName);
        LOG.info("downloaded pkcs12 file name: {}", pkcs12File.getAbsolutePath());

        Assertions.assertTrue(pkcs12File.exists());

*/
/*
        X509Certificate x509Cert = CryptoService.convertPemToCertificate(new String(Files.readAllBytes(pkcs12File.toPath())));

        assertTrue( "Expecting the requested Common Name included in the subject", x509Cert.getSubjectDN().getName().contains(cn) );
*/
    }

    @Test
    public void testCSRSubmitDirectRestrictionViolated() throws GeneralSecurityException, IOException {

        signIn(USER_NAME_USER, USER_PASSWORD_USER);

        validatePresent(LOC_LNK_REQ_CERT_MENUE);
        click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE );

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);

        String cn = "reqTestRestrictionViolated" + System.currentTimeMillis();
        String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
        X500Principal subjectPrincipal = new X500Principal(subject);

//        GeneralName gn = new GeneralName(GeneralName.dNSName, "foo.bar.com");
        GeneralName gn = new GeneralName(GeneralName.iPAddress, "8.8.8.8");
        String csr = buildCSRAsPEM( subjectPrincipal, new GeneralName[]{gn});

        setLongText(LOC_TA_UPLOAD_CONTENT, csr);

        validatePresent(LOC_TEXT_CONTENT_TYPE);

        validatePresent(LOC_SEL_PIPELINE);

        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

        validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
        click(LOC_BTN_REQUEST_CERTIFICATE);


        validatePresent(LOC_TEXT_MESSAGE_NO_IP );
    }


    @Test
	public void testCSRSubmitRACheck() throws GeneralSecurityException, IOException {

		signIn(USER_NAME_USER, USER_PASSWORD_USER);

		validatePresent(LOC_LNK_REQ_CERT_MENUE);
		click(LOC_LNK_REQ_CERT_MENUE);

        validatePresent(LOC_SEL_PIPELINE );

        click(LOC_SEL_PIPELINE);
        selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_RA_ISSUANCE);

        validatePresent(LOC_TA_UPLOAD_CONTENT);

		String cn = "reqTest" + System.currentTimeMillis();
	    String subject = "CN=" + cn + ", O=trustable solutions, C=DE";
	    X500Principal subjectPrincipal = new X500Principal(subject);
	    String csr = buildCSRAsPEM(subjectPrincipal);
        setLongText(LOC_TA_UPLOAD_CONTENT, csr);

	    validatePresent(LOC_TEXT_CONTENT_TYPE);

	    validatePresent(LOC_SEL_PIPELINE);

	    selectOptionByText(LOC_SEL_PIPELINE, PipelineTestConfiguration.PIPELINE_NAME_WEB_RA_ISSUANCE);

	    validatePresent(LOC_BTN_REQUEST_CERTIFICATE);
	    click(LOC_BTN_REQUEST_CERTIFICATE);

	    validatePresent(LOC_TEXT_CSR_HEADER);

	    // switch to RA officer role
		signIn(USER_NAME_RA, USER_PASSWORD_RA);

	    validatePresent(LOC_LNK_REQUESTS_MENUE);
	    click(LOC_LNK_REQUESTS_MENUE);

	    validatePresent(LOC_TEXT_REQUEST_LIST);

	    validatePresent(LOC_SEL_CSR_ATTRIBUTE);
	    selectOptionByText(LOC_SEL_CSR_ATTRIBUTE, "Subject");

	    validatePresent(LOC_SEL_CSR_CHOICE);

	    selectOptionByText(LOC_SEL_CSR_CHOICE, "equals");

	    validatePresent(LOC_INP_CSR_VALUE);
	    setText(LOC_INP_CSR_VALUE, cn);

	    validatePresent(LOC_TD_CSR_ITEM_PENDING);
	    click(LOC_TD_CSR_ITEM_PENDING);

	    validatePresent(LOC_TEXT_CSR_HEADER);

	    By bySubject = By.xpath("//div//dl/dd/span [contains(text(), '"+cn+"')]");
	    validatePresent(bySubject);

	    validatePresent(LOC_BTN_CONFIRM_REQUEST);
	    click(LOC_BTN_CONFIRM_REQUEST);

	    validatePresent(LOC_LNK_REQUESTS_MENUE);
	    click(LOC_LNK_REQUESTS_MENUE);

        waitForElement(LOC_TEXT_REQUEST_LIST);

        validatePresent(LOC_TEXT_REQUEST_LIST);

		signIn(USER_NAME_USER, USER_PASSWORD_USER);

		click(LOC_LNK_CERTIFICATES_MENUE);

	    validatePresent(LOC_TEXT_CERTIFICATE_LIST);


	    validatePresent(LOC_SEL_CERT_ATTRIBUTE);
	    selectOptionByText(LOC_SEL_CERT_ATTRIBUTE, "Subject");

	    validatePresent(LOC_SEL_CERT_CHOICE);

	    selectOptionByText(LOC_SEL_CERT_CHOICE, "equals");

	    validatePresent(LOC_INP_CERT_VALUE);
	    setText(LOC_INP_CERT_VALUE, cn);

	    By byCertSubject = By.xpath("//table//td [contains(text(), '"+cn+"')]");
	    validatePresent(byCertSubject);
	    click(byCertSubject);

	    validatePresent(LOC_TEXT_CERT_HEADER);
		validatePresent(LOC_TEXT_PKIX_LABEL);

/*
		try {
			System.out.println("... waiting ...");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
*/

	}

	private void signIn(final String user, final String password) {

		if( isPresent(LOC_TXT_WEBPACK_ERROR) ) {
			System.err.println(
					"###########################################################\n"+
					"Startup failed, webpack missing. Please build full package.\n"+
			        "###########################################################\n");
		}

		validatePresent(LOC_LNK_ACCOUNT_MENUE);

		// log out, if logged in
		logOut();

		validatePresent(LOC_LNK_ACCOUNT_SIGN_IN_MENUE);
		click(LOC_LNK_ACCOUNT_SIGN_IN_MENUE);

		validatePresent(LOC_LNK_SIGNIN_USERNAME);
		validatePresent(LOC_LNK_SIGNIN_PASSWORD);
		validatePresent(LOC_BTN_SIGNIN_SUBMIT);

		setText(LOC_LNK_SIGNIN_USERNAME, user);
		setText(LOC_LNK_SIGNIN_PASSWORD, password);
		click(LOC_BTN_SIGNIN_SUBMIT);

	}

	private void logOut() {
		validatePresent(LOC_LNK_ACCOUNT_MENUE);

		if( isPresent(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE)) {
			LOG.debug("Logging out ...");
			validatePresent(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE);
			click(LOC_LNK_ACCOUNT_SIGN_OUT_MENUE);
		}else {
			LOG.debug("Already logged out ...");
		}
	}

    public String buildCSRAsPEM( final X500Principal subjectPrincipal ) throws GeneralSecurityException, IOException{
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        return CryptoUtil.getCsrAsPEM(subjectPrincipal,
              keyPair.getPublic(),
                  keyPair.getPrivate(),
                  "password".toCharArray());
    }

    public String buildCSRAsPEM( final X500Principal subjectPrincipal, GeneralName[] sanArray ) throws GeneralSecurityException, IOException{
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        PKCS10CertificationRequest req= CryptoUtil.getCsr(subjectPrincipal,
                keyPair.getPublic(),
                keyPair.getPrivate(),
                "password".toCharArray(),
                null,
                sanArray);
            return CryptoUtil.pkcs10RequestToPem(req);

        }

    public static List<X509Certificate> convertPemToCertificateChain(String pem) throws GeneralSecurityException {
        ByteArrayInputStream pemStream = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
        Reader pemReader = new InputStreamReader(pemStream);
        PEMParser pemParser = new PEMParser(pemReader);

        List<X509Certificate> x509CertificateList = new ArrayList<>();

        try {
            Object parsedObj;
            while((parsedObj = pemParser.readObject())!=null){

                if (parsedObj == null) {
                    throw new GeneralSecurityException("Parsing of certificate failed! Not PEM encoded?");
                }

                LOG.debug("PemParser returned: " + parsedObj);
                if (!(parsedObj instanceof X509CertificateHolder)) {
                    throw new GeneralSecurityException("Unexpected parsing result: " + parsedObj.getClass().getName());
                }

                X509Certificate cert = (new JcaX509CertificateConverter()).setProvider("BC").getCertificate((X509CertificateHolder) parsedObj);
                x509CertificateList.add(cert);
            };
        } catch (IOException var13) {
            LOG.error("IOException, convertPemToCertificate", var13);
            throw new GeneralSecurityException("Parsing of certificate failed! Not PEM encoded?");
        } finally {
            try {
                if(pemParser != null){
                    pemParser.close();
                }
            } catch (IOException var12) {
                LOG.debug("IOException on close()", var12);
            }
        }

        return x509CertificateList;
    }

}
