package de.trustable.ca3s.core.web.servlet;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.servlet.ServletException;

import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.server.ScepServlet;
import org.jscep.transaction.FailInfo;
import org.jscep.transaction.OperationFailureException;
import org.jscep.transaction.TransactionId;
import org.jscep.transport.response.Capability;
import org.jscep.util.CertificationRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;


/**
 * a delegating servlet forwarding the SCEP requests to the backend.
 * 
 * @author ak
 *
 */
@Controller
public class ScepServletImpl extends ScepServlet {
  
    /**
	 * 
	 */
	private static final long serialVersionUID = 7773233909179939491L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ScepServletImpl.class);
    
    private static X500Name pollName = new X500Name("CN=Poll");

    @Autowired
    CertificateRepository certRepository;
    
    @Autowired
    CSRRepository csrRepository;

    @Autowired
    private CSRUtil csrUtil;
    
	@Autowired
	private BPMNUtil bpmnUtil;
	
    @Autowired
    CryptoUtil cryptoUtil;

    @Autowired
    CertificateUtil certUtil;

    @Autowired
	ApplicationContext context;
    
    @Override
    public void init() throws ServletException {   	
    }

    /**
     * 
     * @return
     * @throws ServletException
     */
    Certificate getCurrentRecepientCert() throws ServletException {
    	
    	List<Certificate> certList = certRepository.findByAttributeValue( CertificateAttribute.ATTRIBUTE_SCEP_RECIPIENT, "true");
    	
    	LocalDate now = LocalDate.now();
    	Certificate currentRecepientCert = null;
    	for( Certificate recCert: certList){
    		
    		if( !recCert.isRevoked() && now.isAfter(recCert.getValidFrom())){
    			if( currentRecepientCert == null ){
    				currentRecepientCert = recCert;
    			}else{
    				if( recCert.getValidTo().isAfter(currentRecepientCert.getValidTo())){
        				currentRecepientCert = recCert;
    				}
    			}
    		}
    	}
    	
		if (currentRecepientCert == null) {

			try {
				KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

				X500Principal subject = new X500Principal("CN=SCEPRecepient"
						+ System.currentTimeMillis()
						+ ", O=trustable Ltd, C=DE");
				
				String p10ReqPem = CryptoUtil.getCsrAsPEM(subject, 
						keyPair.getPublic(), 
						keyPair.getPrivate(),
						"password".toCharArray());

				LOGGER.debug("created csr SCEPRecepient '{}'", p10ReqPem );

				TransactionId tid = new TransactionId(new byte[] {1});
			
				currentRecepientCert = startCertificateCreationProcess(p10ReqPem, tid);
				
				certUtil.storePrivateKey(currentRecepientCert, keyPair);			

				certUtil.setCertAttribute(currentRecepientCert, CertificateAttribute.ATTRIBUTE_SCEP_RECIPIENT, "true");

				certRepository.save(currentRecepientCert);

			} catch (GeneralSecurityException | IOException e) {
				throw new ServletException(e);
			}

		}
		
		return currentRecepientCert;
    }

	private Certificate startCertificateCreationProcess(final String csrAsPem, TransactionId transId )  {
		
		// BPNM call
		try {
			Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(csrAsPem);

			CSR csr = csrUtil.buildCSR(csrAsPem, p10ReqHolder);

			CsrAttribute csrAttributeTransId = new CsrAttribute();
			csrAttributeTransId.setName(CertificateAttribute.ATTRIBUTE_SCEP_TRANS_ID);
			csrAttributeTransId.setValue(transId.toString());
			csr.addCsrAttributes(csrAttributeTransId);
			csrRepository.save(csr);

			LOGGER.debug("csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());
			for(de.trustable.ca3s.core.domain.RDN rdn:csr.getRdns()) {
				LOGGER.debug("RDN contains #{}", rdn.getRdnAttributes().size());
			}
			
			Certificate cert = bpmnUtil.startCertificateCreationProcess(csr);
			if(cert != null) {
				LOGGER.debug("new certificate id '{}' for SCEP transaction id '{}'", cert.getId(), transId.toString() );
				
				certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SCEP_TRANS_ID, transId.toString());
				
				certRepository.save(cert);
				
				return cert;
				
			} else {
				LOGGER.warn("creation of certificate by SCEP transaction id '{}' failed ", transId.toString());
			}

			// end of BPMN call

		} catch (GeneralSecurityException | IOException e) {
			LOGGER.warn("execution of CSRProcessingTask failed ", e);
		}

		return null;
	}


	
	
    @Override
    protected List<X509Certificate> doEnrol(PKCS10CertificationRequest csr,
    		X509Certificate sender,
            TransactionId transId) throws OperationFailureException {
    	
        LOGGER.debug("doEnrol(" + csr.toString() + ", " + transId.toString() +")");
        try {
        	
            X500Name subject = X500Name.getInstance(csr.getSubject());
            LOGGER.debug(subject.toString());
            if (subject.equals(pollName)) {
                return Collections.emptyList();
            }
            
            String password = CertificationRequestUtils.getChallengePassword(csr);
            if (!password.equals("password")) {
                LOGGER.debug("Invalid password");
                throw new OperationFailureException(FailInfo.badRequest);
            }
            
            String p10ReqPem = CryptoUtil.pkcs10RequestToPem(csr);
        	Certificate newCertDao = startCertificateCreationProcess(p10ReqPem, transId);
        	if( newCertDao == null ){
                LOGGER.debug("creation of certificate failed");
                throw new OperationFailureException(FailInfo.badRequest);
        	}
        	
    		certUtil.setCertAttribute(newCertDao, CertificateAttribute.ATTRIBUTE_SCEP_TRANS_ID, transId.toString());

			certRepository.save(newCertDao);
            X509Certificate issued = CryptoUtil.convertPemToCertificate(newCertDao.getContent()); 
            
            LOGGER.debug("Issuing {}", issued);

            return Collections.singletonList(issued);
        } catch (Exception e) {
            LOGGER.warn("Error in enrollment", e);
            throw new OperationFailureException(FailInfo.badRequest);
        }
    }

    @Override
    protected List<X509Certificate> doGetCaCertificate(String identifier) {
        LOGGER.debug("doGetCaCertificate(" + identifier +")");
        
        List<X509Certificate> caList = new ArrayList<X509Certificate>();
        try {
			Certificate recepCert = getCurrentRecepientCert();
			caList = certUtil.getX509CertificateChainAsList(recepCert);
		} catch (GeneralSecurityException | ServletException e) {
			LOGGER.warn("Failed to retrieve CA certificates", e);
		}

        return caList;
    }

    @Override
    protected X509CRL doGetCrl(X500Name issuer, BigInteger serial)
            throws OperationFailureException {
        LOGGER.debug("doGetCrl(" + issuer.toString() +", "+ serial.toString(10) +")");
        return null;
    }

    @Override
    protected Set<Capability> doCapabilities(String identifier) {
        LOGGER.debug("doCapabilities(" + identifier +")");
        return EnumSet.of(Capability.RENEWAL,
        		Capability.SCEP_STANDARD, 
        		Capability.SHA_256, Capability.SHA_512, 
        		Capability.POST_PKI_OPERATION);
    }

    @Override
    protected List<X509Certificate> doGetCert(X500Name issuer, BigInteger serial)
            throws OperationFailureException {
    	

        LOGGER.debug("doGetCert(" + issuer.toString() +", "+ serial.toString() +")");

    	List<Certificate> certDaoList = certRepository.findByIssuerSerial(issuer.toString(), serial.toString());

    	if( certDaoList.isEmpty()){
            LOGGER.debug("no match for doGetCert(" + issuer.toString() +", "+ serial.toString() +")");
            
            RDN[] rdns = issuer.getRDNs();
            for( RDN rdn: rdns){
            	AttributeTypeAndValue[] attTVArr = rdn.getTypesAndValues();
                for( AttributeTypeAndValue attTV: attTVArr){
                    LOGGER.debug("AttributeTypeAndValue of issuer :" + attTV.getType().toString() +" = "+ attTV.getValue().toString() );
                }
            }
            RDN[] rdnsIssuer =  issuer.getRDNs(BCStyle.CN);
            if( rdnsIssuer.length > 0){
	            String rdnIssuerString = rdnsIssuer[0].getFirst().getValue().toString();
	            LOGGER.debug("looking for doGetCert(" + rdnIssuerString +", "+ serial.toString() +")");
	            
	            certDaoList = certRepository.findByTermNamed2(CertificateAttribute.ATTRIBUTE_ISSUER, rdnIssuerString, 
	        			CertificateAttribute.ATTRIBUTE_SERIAL, serial.toString());
            }
            
        	if( certDaoList.isEmpty()){
        		throw new OperationFailureException(FailInfo.badCertId);
        	}
    	}
    	
        List<X509Certificate> certList = new ArrayList<X509Certificate>();

        for(Certificate certDao: certDaoList ){
        	try {
        		X509Certificate x509Cert = CryptoUtil.convertPemToCertificate(certDao.getContent());
        		if( x509Cert.getIssuerX500Principal().getName().equals(issuer.toString())){
                    LOGGER.debug("issuer match for doGetCert(" + issuer.toString() +", "+ serial.toString() +")");
        		}
				certList.add(x509Cert);
			} catch (GeneralSecurityException e) {
				LOGGER.warn("decoding certificate failed", e);
	            throw new OperationFailureException(FailInfo.badRequest);
			}
        }
        
        return certList ;
    }

    @Override
    protected List<X509Certificate> doGetCertInitial(X500Name issuer,
            X500Name subject, TransactionId transId)
            throws OperationFailureException {
    	
        LOGGER.debug("doGetCertInitial(" + issuer.toString() +", "+ subject.toString() + ", " + transId.toString() +")");
        
        if (subject.equals(pollName)) {
            return Collections.emptyList();
        }
/*
 * not implemented         
 *
        try {
            return Collections.singletonList( ...);
        } catch (Exception e) {
            LOGGER.info("doGetCertInitial", e);
            throw new OperationFailureException(FailInfo.badCertId);
        }
*/        
        return Collections.emptyList();
    }

    @Override
    protected List<X509Certificate> getNextCaCertificate(String identifier) {
        LOGGER.debug("getNextCaCertificate(" + identifier +")");
/*        
        if (identifier == null || identifier.length() == 0) {
            return Collections.singletonList(ca);
        }
*/        
        return Collections.emptyList();
    }

    @Override
    protected PrivateKey getRecipientKey(){

        try {
			return certUtil.getPrivateKey(getCurrentRecepientCert());
		} catch (ServletException e) {
			LOGGER.warn("problem retrieving recipient's private key", e);
			return null;
		}
    }

    @Override
    protected X509Certificate getRecipient() {
        
        try {
        	X509Certificate ca = CryptoUtil.convertPemToCertificate(getCurrentRecepientCert().getContent());
	        LOGGER.debug("getRecipient() returns " + ca.toString());
	        return ca;
		} catch (GeneralSecurityException | ServletException e) {
			LOGGER.warn("problem retrieving recipient certificate", e);
		}
        return null;
    }

    @Override
    protected PrivateKey getSignerKey() {
        LOGGER.debug("getSignerKey(), returning getRecipientKey()");
        return getRecipientKey();
    }

    @Override
    protected X509Certificate getSigner() {
        LOGGER.debug("getSigner(), returning getRecipient()");
        return getRecipient();
    }
    
    @Override
    protected X509Certificate[] getSignerCertificateChain() {
        LOGGER.debug("getSignerCertificateChain()");
        
        X509Certificate[] signerChainArr = new X509Certificate[0];
        
        try {
			Certificate recepCert = getCurrentRecepientCert();
			
			List<Certificate> certList = certUtil.getCertificateChain(recepCert);
			int chainLength = certList.size();
			if( chainLength > 1) {
				signerChainArr = new X509Certificate[chainLength-1];

				int j = 0;
				for( int i = 1; i < chainLength; i++) {
					signerChainArr[j++] = CryptoService.convertPemToCertificate(certList.get(i).getContent());
				}

			}
		} catch (GeneralSecurityException | ServletException e) {
			LOGGER.warn("Failed to retrieve CA certificates", e);
		}

        return signerChainArr;

    }

}
