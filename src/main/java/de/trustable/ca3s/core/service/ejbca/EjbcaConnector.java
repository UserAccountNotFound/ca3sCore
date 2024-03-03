package de.trustable.ca3s.core.service.ejbca;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.schedule.ImportInfo;
import de.trustable.ca3s.core.service.cmp.SSLSocketFactoryWrapper;
import de.trustable.ca3s.core.service.dto.ejbca.CertificateRestResponseV2;
import de.trustable.ca3s.core.service.dto.ejbca.SearchCertificatesRestResponseV2;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EjbcaConnector {

    Logger LOGGER = LoggerFactory.getLogger(EjbcaConnector.class);

    final long PAGE_SIZE = 10L;
    private final X509TrustManager ca3sTrustManager;

    private final CertificateUtil certUtil;

    public EjbcaConnector(X509TrustManager ca3sTrustManager, CertificateUtil certUtil) {
        this.ca3sTrustManager = ca3sTrustManager;
        this.certUtil = certUtil;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int retrieveCertificates(CAConnectorConfig caConfig) throws IOException {

        ImportInfo importInfo = new ImportInfo();

        if (caConfig.getCaUrl() == null) {
            LOGGER.warn("in retrieveCertificates: url missing");
            return 0;
        }

        if (caConfig.getSelector() == null  || caConfig.getSelector().isEmpty()) {
            LOGGER.warn("in retrieveCertificates: selector missing");
            return 0;
        }

        String certificateRequestQuery = "{\n" +
            "  \"pagination\": {\n" +
            "    \"page_size\": 1000,\n" +
            "    \"current_page\": 1\n" +
            "  }," +
            "  \"criteria\": [\n" +
            "    {\n" +
            "      \"property\": \"UPDATE_TIME\",\n" +
            "      \"value\": \"" + caConfig.getLastUpdate() + "\",\n" +
            "      \"operation\": \"AFTER\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"sort_operation\": {\n" +
            "    \"property\": \"UPDATE_TIME\",\n" +
            "    \"operation\": \"ASC\"\n" +
            "  }\n" +
            "}";


        LOGGER.debug("in retrieveCertificates: query: " + certificateRequestQuery);

        try {
            Certificate certificateTlsAuthentication = caConfig.getTlsAuthentication();

            String url = caConfig.getCaUrl().toLowerCase();
            if (url.startsWith("http://") || certificateTlsAuthentication == null ) {
                importInfo = invokeRestEndpoint(caConfig,
                    importInfo,
                    url,
                    certificateRequestQuery.getBytes(),
                    null, // SNI
                    true, // disableHostNameVerifier
                    null,
                    null
                );
            } else if (url.startsWith("https://")) {
                CertificateUtil.KeyStoreAndPassphrase keyStoreAndPassphrase =
                    certUtil.getContainer(certificateTlsAuthentication,
                        "entryAlias",
                        "passphraseChars".toCharArray(),
                        "PBEWithHmacSHA256AndAES_256");

                importInfo = invokeRestEndpoint(caConfig,
                    importInfo,
                    url,
                    certificateRequestQuery.getBytes(),
                    null, // SNI
                    true, // disableHostNameVerifier
                    keyStoreAndPassphrase.getKeyStore(),
                    new String(keyStoreAndPassphrase.getPassphraseChars()) );
            } else {
                return 0;
            }

        } catch (IOException e) {
            LOGGER.debug("problem retrieving certificates from ejbca", e);
            throw e;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        return importInfo.getImported();
    }


    private ImportInfo invokeRestEndpoint(CAConnectorConfig caConfig,
                                          ImportInfo importInfo,
                                          String requestUrl,
                                          byte[] requestBytes,
                                          final String sni,
                                          final boolean disableHostNameVerifier,
                                          KeyStore keyStore,
                                          String keyPassword) throws IOException {

        LOGGER.debug("Sending request to: " + requestUrl);

        long startTime = System.currentTimeMillis();

        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        if ("https".equals(url.getProtocol())) {
            try {
                KeyManager[] keyManagers = null;
                if (keyStore != null) {
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                    keyManagerFactory.init(keyStore, keyPassword.toCharArray());
                    keyManagers = keyManagerFactory.getKeyManagers();
                    LOGGER.debug("using client keystore");
                }

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(keyManagers,
                    new TrustManager[]{ca3sTrustManager},
                    new java.security.SecureRandom());

                SSLSocketFactory socketFactory = sc.getSocketFactory();
                if (sni != null && !sni.trim().isEmpty()) {
                    LOGGER.debug("using sni '{}' for CA '{}'", sni, requestUrl);
                    SSLParameters sslParameters = new SSLParameters();
                    List sniHostNames = new ArrayList(1);
                    sniHostNames.add(new SNIHostName(sni));
                    sslParameters.setServerNames(sniHostNames);
                    socketFactory = new SSLSocketFactoryWrapper(socketFactory, sslParameters);
                }
                HttpsURLConnection conTLS = (HttpsURLConnection) con;

                if (disableHostNameVerifier) {
                    conTLS.setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                }

                conTLS.setSSLSocketFactory(socketFactory);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IOException("problem configuring the SSLContext", e);
            } catch (
                UnrecoverableKeyException e) {
                throw new IOException("problem reading keystore", e);
            } catch (
                KeyStoreException e) {
                throw new RuntimeException(e);
            }

        } else if ("http".equals(url.getProtocol())) {
            // everything's fine, nothing to do ...
        } else {
            throw new IOException("Unexpected protocol '" + url.getProtocol() + "'");
        }

        // we are going to do a POST
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        java.io.OutputStream os = con.getOutputStream();
        os.write(requestBytes);
        os.close();

        // Read the response
        parseResponse( con.getInputStream(), importInfo, caConfig );

        if (con.getResponseCode() == 200) {
            LOGGER.debug("Received certificate reply.");
        } else {
            throw new IOException("Error sending CMP request. Response code != 200 : " + con.getResponseCode());
        }

        // We are done, disconnect
        con.disconnect();

        LOGGER.debug("duration of remote EJBCA inventory call " + (System.currentTimeMillis() - startTime));

        return importInfo;
    }

    void parseResponse( InputStream in, ImportInfo importInfo,final CAConnectorConfig caConfig ){

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SearchCertificatesRestResponseV2 certificateSearchResponse = objectMapper.readValue(in, SearchCertificatesRestResponseV2.class);

            for(CertificateRestResponseV2 certificateRestResponse : certificateSearchResponse.getCertificates()){

                String desc = ( certificateRestResponse.getSubjectDn() == null ? "" : certificateRestResponse.getSubjectDn() ) + ", #" + certificateRestResponse.getSerialNumber();
                if( !certificateRestResponse.getBase64Cert().isEmpty() ){

                    CSR csr = null;
                    if( !certificateRestResponse.getCertificateRequest().isEmpty() ) {
                        LOGGER.info("CertificateRestResponseV2 contains csr!");
                    }

                    byte[] certBytes = Base64.decodeBase64(certificateRestResponse.getBase64Cert().get(0));
                    try {
                        Certificate certificate = certUtil.createCertificate(certBytes, csr, "", true, caConfig.getCaUrl());
                        Instant lastUpdateInstant = Instant.ofEpochMilli( certificateRestResponse.getUdpateTime() );
                        if( lastUpdateInstant.isAfter(caConfig.getLastUpdate())){
                            caConfig.setLastUpdate(lastUpdateInstant);
                        }
                        importInfo.incImported();
                    } catch (GeneralSecurityException e) {
                        LOGGER.info("CertificateRestResponseV2: parsing certificate for '{}' failed: {} ", desc, e.getMessage());
                    }

                }else{
                    LOGGER.info("CertificateRestResponseV2 does not contain base64 data for {}", desc);
                }
            }

        } catch (IOException e) {
            LOGGER.info("unmarshalling CertificateRestResponseV2", e);
        }

    }
}
