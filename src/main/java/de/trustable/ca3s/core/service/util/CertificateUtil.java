package de.trustable.ca3s.core.service.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.provider.JCEECPublicKey;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.RDNAttribute;
import de.trustable.ca3s.core.domain.RequestAttribute;
import de.trustable.ca3s.core.domain.RequestAttributeValue;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.CertificateAttributeRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;


@Service
public class CertificateUtil {

	private static final String SERIAL_PADDING_PATTERN = "000000000000000000000";

	static HashSet<Integer> lenSet = new HashSet<Integer>();
	static {
		lenSet.add(256);
		lenSet.add(512);
		lenSet.add(1024);
		lenSet.add(2048);
		lenSet.add(3072);
		lenSet.add(4096);
		lenSet.add(6144);
		lenSet.add(8192);
	}


	private static final Logger LOG = LoggerFactory.getLogger(CertificateUtil.class);

	@Autowired
	private CertificateRepository certificateRepository;

	@Autowired
	private CertificateAttributeRepository certificateAttributeRepository;

	@Autowired
	private ProtectedContentRepository protContentRepository;
	
	@Autowired
	private ProtectedContentUtil protUtil;
	
	@Autowired
	private CryptoService cryptoUtil;

	
    public Certificate createCertificate(final byte[] encodedCert, final CSR csr, final String executionId, final boolean reimport) throws GeneralSecurityException, IOException {

    	try {
	    	CertificateFactory factory = CertificateFactory.getInstance("X.509");
	    	X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(encodedCert));
	    	
	    	String pemCert = cryptoUtil.x509CertToPem(cert);
	
	        return createCertificate(pemCert, csr, executionId, reimport);
    	} catch (GeneralSecurityException | IOException e) {
    		throw e;
    	} catch (Throwable th) {
    		throw new GeneralSecurityException("problem importing certificate: " + th.getMessage());
    	}
    }

	/**
	 * 
	 * @param pemCert
	 * @param csr
	 * @param executionId
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Certificate createCertificate(final String pemCert, final CSR csr, 
			final String executionId) throws GeneralSecurityException, IOException {
		
		return createCertificate(pemCert, csr, executionId, false);
	}

	/**
	 * 
	 * @param pemCert
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Certificate getCertificateByPEM(final String pemCert) throws GeneralSecurityException, IOException {
		X509Certificate x509Cert = CryptoService.convertPemToCertificate(pemCert);
		return getCertificateByX509(x509Cert);
		
	}
	
	/**
	 * 
	 * @param x509Cert
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Certificate getCertificateByX509(final X509Certificate x509Cert) throws GeneralSecurityException, IOException {

		String tbsDigestBase64 = Base64.encodeBase64String(cryptoUtil.getSHA256Digest(x509Cert.getTBSCertificate())).toLowerCase();
		List<Certificate> certList = certificateRepository.findByTBSDigest(tbsDigestBase64);

		if (certList.isEmpty()) {
			return null;
		} else {
			return certList.get(0);
		}
	}
	
	/**
	 * 
	 * @param pemCert
	 * @param csr
	 * @param executionId
	 * @param reimport
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Certificate createCertificate(final String pemCert, final CSR csr, 
			final String executionId,
			final boolean reimport) throws GeneralSecurityException, IOException {


		X509Certificate x509Cert = CryptoService.convertPemToCertificate(pemCert);
		Certificate cert = getCertificateByX509(x509Cert);

		if (cert  == null) {
			String tbsDigestBase64 = Base64.encodeBase64String(cryptoUtil.getSHA256Digest(x509Cert.getTBSCertificate())).toLowerCase();
			cert = createCertificate(pemCert, csr, executionId, x509Cert, tbsDigestBase64);
		} else {
			LOG.info("certificate '" + cert .getSubject() +"' already exists");

			if( reimport ) {
				LOG.debug("existing certificate '" + cert .getSubject() +"' overwriting some attributes, only");
				addAdditionalCertificateAttributes(x509Cert, cert);
			}
		}
		return cert;
	}

	/**
	 * @param pemCert
	 * @param csr
	 * @param executionId
	 * @param x509Cert
	 * @param tbsDigestBase64
	 * @return
	 * @throws CertificateEncodingException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateParsingException
	 * @throws CertificateException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException
	 * @throws SignatureException
	 */
	private Certificate createCertificate(final String pemCert, final CSR csr, final String executionId,
			X509Certificate x509Cert, String tbsDigestBase64)
			throws CertificateEncodingException, IOException, NoSuchAlgorithmException, CertificateParsingException,
			CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
		
		Certificate cert;
		LOG.debug("creating new certificate '" + x509Cert.getSubjectDN().getName() +"'");
		
		byte[] certBytes = x509Cert.getEncoded();
		X509CertificateHolder x509CertHolder = new X509CertificateHolder(certBytes);
		
		cert = new Certificate();
		cert.setCertificateAttributes(new HashSet<CertificateAttribute>());

		String type = "X509V" + x509Cert.getVersion();
		cert.setType(type);

		String serial = x509Cert.getSerialNumber().toString();
		cert.setSerial(serial);

		cert.setContent(pemCert);

		if( csr != null) {
			// do not overwrite an existing CSR
			cert.setCsr(csr);
		}
		
		// indexed key for searching
		cert.setTbsDigest(tbsDigestBase64);

		// derive a readable description
		String desc = cryptoUtil.getDescription(x509Cert);
		cert.setDescription(CryptoService.limitLength(desc, 250));


		// good old SHA1 fingerprint
		String fingerprint = Base64.encodeBase64String(generateSHA1Fingerprint(certBytes));
		cert.setFingerprint(fingerprint);

		cert.setValidFrom( DateUtil.asInstant(x509Cert.getNotBefore()));
		cert.setValidTo(DateUtil.asInstant(x509Cert.getNotAfter()));

		//initialize revocation details
		cert.setRevokedSince(null);
		cert.setRevocationReason(null);
		cert.setRevoked(false);

		if (executionId != null) {
			cert.setCreationExecutionId(executionId);
		}

		cert.setContentAddedAt(Instant.now());

		String issuer = CryptoService.limitLength(x509Cert.getIssuerDN().getName(), 250);
		cert.setIssuer(issuer);

		String subject = CryptoService.limitLength(x509Cert.getSubjectDN().getName(), 250);
		cert.setSubject(subject);

		cert.setSelfsigned(false);

		certificateRepository.save(cert);

		//
		// write certificate attributes
		//
		
		// guess some details from basic constraint
		int basicConstraint = x509Cert.getBasicConstraints();
		if (Integer.MAX_VALUE == basicConstraint) {
			cert.setEndEntity(true);
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CA, "true");
		} else if (-1 == basicConstraint) {
			cert.setEndEntity(true);
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_END_ENTITY, "true");
		} else {
			cert.setEndEntity(true);
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CA, "true");
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CHAIN_LENGTH, "" + basicConstraint);
		}


		// add the basic key usages a attributes
		usageAsCertAttributes( x509Cert.getKeyUsage(), cert );
		
		// add the extended key usages a attributes
		List<String> extKeyUsageList = x509Cert.getExtendedKeyUsage();
		if (extKeyUsageList != null) {
			for (String extUsage : extKeyUsageList) {
				setCertMultiValueAttribute(cert, OidNameMapper.lookupOid(extUsage), extUsage);
			}
		}


		setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ISSUER, issuer.toLowerCase());

		X500Name x500NameIssuer = x509CertHolder.getIssuer();
		insertNameAttributes(cert, CertificateAttribute.ATTRIBUTE_ISSUER, x500NameIssuer);

		setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SUBJECT, subject.toLowerCase());

		X500Name x500NameSubject = x509CertHolder.getSubject();
		insertNameAttributes(cert, CertificateAttribute.ATTRIBUTE_SUBJECT, x500NameSubject);

		setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_TYPE, type);


		JcaX509ExtensionUtils util = new JcaX509ExtensionUtils();
		
		// build two SKI variants for cert identification
		SubjectKeyIdentifier ski = util.createSubjectKeyIdentifier(x509Cert.getPublicKey());
		String b46Ski = Base64.encodeBase64String(ski.getKeyIdentifier());
		
		setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SKI,b46Ski);
		
		SubjectKeyIdentifier skiTruncated = util.createTruncatedSubjectKeyIdentifier(x509Cert.getPublicKey());
		if( !ski.equals(skiTruncated)){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SKI,
					Base64.encodeBase64String(skiTruncated.getKeyIdentifier()));
		}
		
		// add two serial variants
		setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SERIAL, serial);
		setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SERIAL_PADDED, getPaddedSerial(serial));

		// add validity period
		setCertAttribute(cert,
				CertificateAttribute.ATTRIBUTE_VALID_FROM_TIMESTAMP, ""
						+ x509Cert.getNotBefore().getTime());

		setCertAttribute(cert,
				CertificateAttribute.ATTRIBUTE_VALID_TO_TIMESTAMP, ""
						+ x509Cert.getNotAfter().getTime());

		long validityPeriod = ( x509Cert.getNotAfter().getTime() - x509Cert.getNotBefore().getTime() ) / 1000L;
		setCertAttribute(cert,
				CertificateAttribute.ATTRIBUTE_VALIDITY_PERIOD, "" + validityPeriod);
		
		addAdditionalCertificateAttributes(x509Cert, cert);

		certificateRepository.save(cert);
		certificateAttributeRepository.saveAll(cert.getCertificateAttributes());

		if( x500NameIssuer.equals(x500NameSubject) ){
			
			// check whether is really selfsigned 
			x509Cert.verify(x509Cert.getPublicKey());

			// don't insert the self-reference. This leads to no good when JSON-serializing the object 
			// The selfsigned-attribute will mark the fact!
			// cert.setIssuingCertificate(cert);
			
			// mark it as self signed
			cert.setSelfsigned(true);
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SELFSIGNED, "true");
			cert.setIssuingCertificate(cert);
			
			cert.setRootCertificate(cert);
			cert.setRoot(cert.getSubject());
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ROOT, cert.getSubject().toLowerCase());

			LOG.debug("certificate '" + x509Cert.getSubjectDN().getName() +"' is selfsigned");
			
		}else{
			// try to build cert chain
			try{
				Certificate issuingCert = findIssuingCertificate(x509CertHolder);
				
				if( issuingCert == null ) {
					LOG.info("unable to find issuer for non-self-signed certificate '" + x509Cert.getSubjectDN().getName() +"' right now ...");
				}else {
					cert.setIssuingCertificate(issuingCert);
					if( LOG.isDebugEnabled()){
						LOG.debug("certificate '" + x509Cert.getSubjectDN().getName() +"' issued by " + issuingCert.getSubject());
					}
				}

				Certificate rootCert = findRootCertificate(issuingCert);
				cert.setRootCertificate(rootCert);
				cert.setRoot(rootCert.getSubject());
				setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ROOT, rootCert.getSubject().toLowerCase());

			} catch( GeneralSecurityException gse){
//				LOG.debug("exception while retrieving issuer", gse);
				LOG.info("problem retrieving issuer for certificate '" + x509Cert.getSubjectDN().getName() +"' right now ...");
			}
		}

		
		certificateRepository.save(cert);
//		LOG.debug("certificate id '" + cert.getId() +"' post-save");
		certificateAttributeRepository.saveAll(cert.getCertificateAttributes());
		LOG.debug("certificate id '{}' saved containing #{} attributes", cert.getId(), cert.getCertificateAttributes().size());
		for( CertificateAttribute cad: cert.getCertificateAttributes()){
			LOG.debug("Name '" + cad.getName() +"' got value '" + cad.getValue() + "'");
		}

		return cert;
	}


	/**
	 * @param x509Cert
	 * @param cert
	 * @throws CertificateParsingException
	 */
	private void addAdditionalCertificateAttributes(X509Certificate x509Cert, Certificate cert)
			throws CertificateParsingException {
		
		// extract signature algo
		String sigAlgName = x509Cert.getSigAlgName().toLowerCase();
		
		cert.setSigningAlgorithm(sigAlgName);

		String keyAlgName = sigAlgName;
		String hashAlgName = "undefined";
		String paddingAlgName = "PKCS1"; // assume a common default padding
		
		if( sigAlgName.contains("with")) {
			String[] parts = sigAlgName.split("with");
			if(parts.length > 1) {
				hashAlgName = parts[0];
				if(parts[1].contains("and")) {
					String[] parts2 = parts[1].split("and");
					keyAlgName = parts2[0];
					if(parts2.length > 1) {
						paddingAlgName = parts2[1];
					}
				}else {
					keyAlgName = parts[1];
				}
			}
		}

		cert.setKeyAlgorithm(keyAlgName);
		cert.setHashingAlgorithm(hashAlgName);
		cert.setPaddingAlgorithm(paddingAlgName);
		cert.setSigningAlgorithm(sigAlgName);
		
		try {
			String curveName = deriveCurveName(x509Cert.getPublicKey());
			LOG.info("found curve name "+ curveName +" for certificate '" + x509Cert.getSubjectDN().getName() +"' with key algo " + keyAlgName);
			
			cert.setCurveName(curveName);
			
		} catch (GeneralSecurityException e) {
			if( keyAlgName.contains("ec")) {
				LOG.info("unable to derive curve name for certificate '" + x509Cert.getSubjectDN().getName() +"' with key algo " + keyAlgName);
			}
		}

		// list all SANs
		if (x509Cert.getSubjectAlternativeNames() != null) {
			Collection<List<?>> altNames = x509Cert.getSubjectAlternativeNames();
			
			if( altNames != null) {
				for (List<?> altName : altNames) {
	                Integer altNameType = (Integer) altName.get(0);
	                if (altNameType != 2 && altNameType != 7) { // dns or ip
	                    continue;
	                }
	                setCertMultiValueAttribute(cert, CertificateAttribute.ATTRIBUTE_SAN, ((String)altName.get(1)).toLowerCase());
				}
			}
		}

		int keyLength = getAlignedKeyLength(x509Cert.getPublicKey());
		cert.setKeyLength(keyLength);
		
	}

	public static int getAlignedKeyLength(final PublicKey pk) {
		int keyLength = getKeyLength(pk);
		if( lenSet.contains(keyLength + 1) ) {
			return keyLength + 1;
		}
		if( lenSet.contains(keyLength + 2) ) {
			return keyLength + 2;
		}
		return keyLength;
	}	
	/**
	 * Gets the key length of supported keys
	 * @param pk PublicKey used to derive the keysize
	 * @return -1 if key is unsupported, otherwise a number >= 0. 0 usually means the length can not be calculated, 
	 * for example if the key is an EC key and the "implicitlyCA" encoding is used.
	 */
	public static int getKeyLength(final PublicKey pk) {
	    int len = -1;
	    if (pk instanceof RSAPublicKey) {
	        final RSAPublicKey rsapub = (RSAPublicKey) pk;
	        len = rsapub.getModulus().bitLength();
	    } else if (pk instanceof JCEECPublicKey) {
	        final JCEECPublicKey ecpriv = (JCEECPublicKey) pk;
	        final org.bouncycastle.jce.spec.ECParameterSpec spec = ecpriv.getParameters();
	        if (spec != null) {
	            len = spec.getN().bitLength();              
	        } else {
	            // We support the key, but we don't know the key length
	            len = 0;
	        }
	    } else if (pk instanceof ECPublicKey) {
	        final ECPublicKey ecpriv = (ECPublicKey) pk;
	        final java.security.spec.ECParameterSpec spec = ecpriv.getParams();
	        if (spec != null) {
	            len = spec.getOrder().bitLength(); // does this really return something we expect?
	        } else {
	            // We support the key, but we don't know the key length
	            len = 0;
	        }
	    } else if (pk instanceof DSAPublicKey) {
	        final DSAPublicKey dsapub = (DSAPublicKey) pk;
	        if ( dsapub.getParams() != null ) {
	            len = dsapub.getParams().getP().bitLength();
	        } else {
	            len = dsapub.getY().bitLength();
	        }
	    } 
	    return len;
	}
	
	/**
	 * derive the curve name
	 * 
	 * @param ecParameterSpec
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static final String deriveCurveName(org.bouncycastle.jce.spec.ECParameterSpec ecParameterSpec)
			throws GeneralSecurityException {
		for (@SuppressWarnings("rawtypes")
		Enumeration names = ECNamedCurveTable.getNames(); names.hasMoreElements();) {
			final String name = (String) names.nextElement();

			final X9ECParameters params = ECNamedCurveTable.getByName(name);

			if (params.getN().equals(ecParameterSpec.getN()) && params.getH().equals(ecParameterSpec.getH())
					&& params.getCurve().equals(ecParameterSpec.getCurve())
					&& params.getG().equals(ecParameterSpec.getG())) {
				return name;
			}
		}

		throw new GeneralSecurityException("Could not find name for curve");
	}

	public static final String deriveCurveName(PublicKey publicKey) throws GeneralSecurityException{
	    if(publicKey instanceof java.security.interfaces.ECPublicKey){
	        final java.security.interfaces.ECPublicKey pk = (java.security.interfaces.ECPublicKey) publicKey;
	        final ECParameterSpec params = pk.getParams();
	        return deriveCurveName(EC5Util.convertSpec(params, false));
	    } else if(publicKey instanceof org.bouncycastle.jce.interfaces.ECPublicKey){
	        final org.bouncycastle.jce.interfaces.ECPublicKey pk = (org.bouncycastle.jce.interfaces.ECPublicKey) publicKey;
	        return deriveCurveName(pk.getParameters());
	    } else throw new GeneralSecurityException("Can only be used with instances of ECPublicKey (either jce or bc implementation)");
	}

	public static final String deriveCurveName(PrivateKey privateKey) throws GeneralSecurityException{
	    if(privateKey instanceof java.security.interfaces.ECPrivateKey){
	        final java.security.interfaces.ECPrivateKey pk = (java.security.interfaces.ECPrivateKey) privateKey;
	        final ECParameterSpec params = pk.getParams();
	        return deriveCurveName(EC5Util.convertSpec(params, false));
	    } else if(privateKey instanceof org.bouncycastle.jce.interfaces.ECPrivateKey){
	        final org.bouncycastle.jce.interfaces.ECPrivateKey pk = (org.bouncycastle.jce.interfaces.ECPrivateKey) privateKey;
	        return deriveCurveName(pk.getParameters());
	    } else throw new GeneralSecurityException("Can only be used with instances of ECPrivateKey (either jce or bc implementation)");
	}


	
	public void insertNameAttributes(Certificate cert, String attributeName, X500Name x500NameSubject) {
		for( RDN rdn: x500NameSubject.getRDNs() ){
			for( org.bouncycastle.asn1.x500.AttributeTypeAndValue atv: rdn.getTypesAndValues()){
				String value = atv.getValue().toString().toLowerCase();
				setCertMultiValueAttribute(cert, attributeName, value);
				setCertMultiValueAttribute(cert, attributeName, atv.getType().getId().toLowerCase() +"="+ value);
			}
		}
	}

	public String getCertAttribute(Certificate certDao, String name) {
		for( CertificateAttribute certAttr:certDao.getCertificateAttributes()) {
			if( certAttr.getName().equals(name)) {
				return certAttr.getValue();
			}
		}
		return null;
	}

	public void setCertAttribute(Certificate certDao, String name, long value) {
		setCertAttribute(certDao, name, Long.toString(value));
	}

	public void setCertMultiValueAttribute(Certificate cert, String name, String value) {
		setCertAttribute(cert, name, value, true);
	}
	
	public void setCertAttribute(Certificate cert, String name, String value) {
		setCertAttribute(cert, name, value, true);
	}
	
	public void setCertAttribute(Certificate cert, String name, String value, boolean multiValue) {
		
		if( name == null) {
			LOG.warn("no use to insert attribute with name 'null'", new Exception());
			return;
		}
		if( value == null) {
			value= "";
		}
		
		
		
		Collection<CertificateAttribute> certAttrList = cert.getCertificateAttributes();
		for( CertificateAttribute certAttr : certAttrList) {

//	        LOG.debug("checking certificate attribute '{}' containng value '{}'", certAttr.getName(), certAttr.getValue());

			if( name.equals(certAttr.getName())) {
				if( value.equals(certAttr.getValue())) {
					// attribute already present, no use in duplication here
					return;
				}else {
					if( !multiValue ) {
						certAttr.setValue(value);
						return;
					}
				}
			}
		}
		
		CertificateAttribute cAtt = new CertificateAttribute();
		cAtt.setCertificate(cert);
		cAtt.setName(name);
		cAtt.setValue(value);
		
		cert.getCertificateAttributes().add(cAtt);
		
		certificateAttributeRepository.save(cAtt);

	}

	
	/**
	 * 
	 * @param startCertDao
	 * @return
	 * @throws GeneralSecurityException
	 */
	public List<Certificate> getCertificateChain(final Certificate startCertDao) throws GeneralSecurityException {
		
		int MAX_CHAIN_LENGTH = 10;
		ArrayList<Certificate> certChain = new ArrayList<Certificate>();
		
		Certificate certDao = startCertDao;
		LOG.debug("added end entity cert id {} to the chain", certDao.getId());
		certChain.add(certDao);
		
		for( int i = 0; i <= MAX_CHAIN_LENGTH; i++ ) {
			
			if( i == MAX_CHAIN_LENGTH) {
				String msg = "maximum chain length ecxeeded for  cert id : " + startCertDao.getId();
				LOG.info(msg);
				throw new GeneralSecurityException(msg);
			}
			
			// walk up the certificate chain
			Certificate issuingCertDao;
			try {
				issuingCertDao = findIssuingCertificate(certDao);

				if( issuingCertDao == null) {
					String msg = "no issuing certificate available / retrievable for cert id : " + certDao.getId();
					LOG.info(msg);
					throw new GeneralSecurityException(msg);
				}else {
					LOG.debug("added issuing cert id {} to the chain", issuingCertDao.getId());
					certChain.add(issuingCertDao);
				}
			} catch (GeneralSecurityException e) {
				String msg = "Error retrieving issuing certificate for cert id : " + certDao.getId();
				LOG.info(msg);
				throw new GeneralSecurityException(msg);
			}

			if( issuingCertDao.getIssuingCertificate() == null) {
				String msg = "no issuing certificate available / retrievable for cert id : " + issuingCertDao.getId();
				LOG.info(msg);
				break;
//				throw new GeneralSecurityException(msg);
			}else {

				// root reached? No need to move further ..
				if( issuingCertDao.getId() == issuingCertDao.getIssuingCertificate().getId()) {
					LOG.debug("certificate chain complete, cert id '{}' is selfsigned", issuingCertDao.getId());
					break;
				}
			}
			
			certDao = issuingCertDao;
		}

		return certChain;
	}

	/**
	 * 
	 * @param startCertDao
	 * @return
	 * @throws GeneralSecurityException
	 */
	public X509Certificate[] getX509CertificateChain(final Certificate startCert) throws GeneralSecurityException {
		
		List<Certificate> certList = getCertificateChain(startCert);
		
		X509Certificate[] chainArr = new X509Certificate[certList.size()];
		for( int i = 0; i < certList.size(); i++) {

			X509Certificate x509Cert = CryptoService.convertPemToCertificate(certList.get(i).getContent());
			chainArr[i] = x509Cert;
		}

		return chainArr;
	}
	
	/**
	 * 
	 * @param startCertDao
	 * @return
	 * @throws GeneralSecurityException
	 */
	public List<X509Certificate> getX509CertificateChainAsList(final Certificate startCert) throws GeneralSecurityException {
		
		List<Certificate> certList = getCertificateChain(startCert);
		
		List<X509Certificate> x509chainList = new ArrayList<X509Certificate>();
		for( int i = 0; i < certList.size(); i++) {

			X509Certificate x509Cert = CryptoService.convertPemToCertificate(certList.get(i).getContent());
			x509chainList.add(x509Cert);
		}

		return x509chainList;
	}

	/**
	 * 
	 * @param serial
	 * @return
	 */
	public static String getPaddedSerial(final String serial){
	
		int len = serial.length();
		if( len >= SERIAL_PADDING_PATTERN.length() ){
			return serial;
		}
		return SERIAL_PADDING_PATTERN.substring(serial.length()) + serial; 
	}
	
    /**
     * Generate a SHA1 fingerprint from a byte array containing a X.509 certificate
     *
     * @param ba Byte array containing DER encoded X509Certificate.
     * @return Byte array containing SHA1 hash of DER encoded certificate.
     */
    public static byte[] generateSHA1Fingerprint(byte[] ba) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return md.digest(ba);
        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("SHA1 algorithm not supported", nsae);
        }
        return null;
    } // generateSHA1Fingerprint


	/**
	 * convert the usage-bits to a readable string
	 * @param usage
	 * @return descriptive text representing the key usage
	 */
	public static String usageAsString( boolean[] usage ){

		if( ( usage == null ) || ( usage.length == 0 ) ){
			return( "unspecified usage" );
		}

		String desc = "valid for ";
		if ( (usage.length > 0) && usage[0]) desc += "digitalSignature ";
		if ( (usage.length > 1) && usage[1]) desc += "nonRepudiation ";
		if ( (usage.length > 2) && usage[2]) desc += "keyEncipherment ";
		if ( (usage.length > 3) && usage[3]) desc += "dataEncipherment ";
		if ( (usage.length > 4) && usage[4]) desc += "keyAgreement ";
		if ( (usage.length > 5) && usage[5]) desc += "keyCertSign ";
		if ( (usage.length > 6) && usage[6]) desc += "cRLSign ";
		if ( (usage.length > 7) && usage[7]) desc += "encipherOnly ";
		if ( (usage.length > 8) && usage[8]) desc += "decipherOnly ";

		return (desc);
	}

	/**
	 * convert the usage-bits to a readable string
	 * @param usage
	 * @return descriptive text representing the key usage
	 */
	public void usageAsCertAttributes( boolean[] usage, Certificate cert ){

		if( ( usage == null ) || ( usage.length == 0 ) ){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "unspecified" );
			return;
		}

		if ( (usage.length > 0) && usage[0]){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "digitalSignature ");
		}
		if ( (usage.length > 1) && usage[1]){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "nonRepudiation ");
		}
		if ( (usage.length > 2) && usage[2]){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "keyEncipherment ");
		}
		if ( (usage.length > 3) && usage[3]){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "dataEncipherment ");
		}
		if ( (usage.length > 4) && usage[4]){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "keyAgreement ");
		}
		if ( (usage.length > 5) && usage[5]){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "keyCertSign ");
		}
		if ( (usage.length > 6) && usage[6]){
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "cRLSign ");
		}
		if ( (usage.length > 7) && usage[7]) {
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "encipherOnly ");
		}
		if ( (usage.length > 8) && usage[8]) {
			setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USAGE,  "decipherOnly ");
		}

	}

	
	public Certificate findIssuingCertificate(Certificate cert) throws GeneralSecurityException {
		
		if( "true".equalsIgnoreCase( getCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SELFSIGNED))) {
			// no need for lengthy calculations, we do know the issuer, yet
			return cert;
		}
		
		Certificate issuingCert = cert.getIssuingCertificate();
		if( issuingCert == null){
			issuingCert = findIssuingCertificate(convertPemToCertificateHolder(cert.getContent()));
			if( issuingCert != null ){
				if( issuingCert.equals(cert)) {
					LOG.warn("found untagged self-signed certificate id '{}', '{}'", cert.getId(), cert.getDescription());
					return cert;
				}
				cert.setIssuingCertificate(issuingCert);
				certificateRepository.save(cert);
			}else {
				LOG.debug("not able to find and store issuing certificate for '" + cert.getDescription() + "'");
			}
		}
		return issuingCert;
	}

	  /**
	   * 
	   * @param pem
	   * @return
	   * @throws GeneralSecurityException
	   */
	  public X509CertificateHolder convertPemToCertificateHolder (final String pem) throws GeneralSecurityException {
		  
		X509Certificate x509Cert = convertPemToCertificate (pem);
		try {
			return new X509CertificateHolder(x509Cert.getEncoded());
		} catch (IOException e) {
			throw new GeneralSecurityException(e);
		}
		
	  }
	  
		/**
		 * 
		 * @param pem
		 * @return
		 * @throws GeneralSecurityException
		 */
		public X509Certificate convertPemToCertificate(final String pem)
				throws GeneralSecurityException {

			X509Certificate cert = null;
			ByteArrayInputStream pemStream = null;
			try {
				pemStream = new ByteArrayInputStream(pem.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				LOG.error("UnsupportedEncodingException, convertPemToPublicKey",
						ex);
				throw new GeneralSecurityException(
						"Parsing of PublicKey failed due to encoding problem! Not PEM encoded?");
			}

			Reader pemReader = new InputStreamReader(pemStream);
			PEMParser pemParser = new PEMParser(pemReader);

			try {
				Object parsedObj = pemParser.readObject();

				if (parsedObj == null) {
					throw new GeneralSecurityException(
							"Parsing of certificate failed! Not PEM encoded?");
				}

//				LOG.debug("PemParser returned: " + parsedObj);

				if (parsedObj instanceof X509CertificateHolder) {
					cert = new JcaX509CertificateConverter().setProvider("BC")
							.getCertificate((X509CertificateHolder) parsedObj);

				} else {
					throw new GeneralSecurityException(
							"Unexpected parsing result: "
									+ parsedObj.getClass().getName());
				}
			} catch (IOException ex) {
				LOG.error("IOException, convertPemToCertificate", ex);
				throw new GeneralSecurityException(
						"Parsing of certificate failed! Not PEM encoded?");
			} finally {
				try {
					pemParser.close();
				} catch (IOException e) {
					// just ignore
					LOG.debug("IOException on close()", e);
				}
			}

			return cert;
		}

		/**
		 * 
		 * @param pem
		 * @return
		 * @throws GeneralSecurityException
		 */
		public PrivateKey convertPemToPrivateKey(final String pem)
				throws GeneralSecurityException {

			PrivateKey privKey = null;
			ByteArrayInputStream pemStream = null;
			try {
				pemStream = new ByteArrayInputStream(pem.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				LOG.error("UnsupportedEncodingException, PrivateKey", ex);
				throw new GeneralSecurityException(
						"Parsing of PEM file failed due to encoding problem! Not PEM encoded?");
			}

			Reader pemReader = new InputStreamReader(pemStream);
			PEMParser pemParser = new PEMParser(pemReader);

			try {
				Object parsedObj = pemParser.readObject();

				if (parsedObj == null) {
					throw new GeneralSecurityException(
							"Parsing of certificate failed! Not PEM encoded?");
				}

//				LOG.debug("PemParser returned: " + parsedObj);

				if (parsedObj instanceof PrivateKeyInfo) {
					privKey = new JcaPEMKeyConverter().setProvider("BC")
							.getPrivateKey((PrivateKeyInfo) parsedObj);
				} else {
					throw new GeneralSecurityException(
							"Unexpected parsing result: "
									+ parsedObj.getClass().getName());
				}

			} catch (IOException ex) {
				LOG.error("IOException, convertPemToCertificate", ex);
				throw new GeneralSecurityException(
						"Parsing of certificate failed! Not PEM encoded?");
			} finally {
				try {
					pemParser.close();
				} catch (IOException e) {
					// just ignore
					LOG.debug("IOException on close()", e);
				}
			}

			return privKey;
		}
 
	/**
	 * 
	 * @param x509CertHolder
	 * @return
	 * @throws GeneralSecurityException
	 */
	public Certificate findIssuingCertificate(X509CertificateHolder x509CertHolder) throws GeneralSecurityException {

		Objects.requireNonNull(x509CertHolder, "x509CertHolder can't be null");

		List<Certificate> issuingCertList = new ArrayList<Certificate>();

		// lokk for the AKI extension in the given certificate
		if( (x509CertHolder != null) && (x509CertHolder.getExtensions() != null)) {
			AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.fromExtensions(x509CertHolder.getExtensions());
			if( aki != null) {
				issuingCertList = findCertsByAKI(x509CertHolder, aki);
			}
		}

		if( issuingCertList.isEmpty()){			
			LOG.debug("AKI from crt extension failed, trying to find issuer name");
			issuingCertList = certificateRepository.findCACertByIssuer(x509CertHolder.getIssuer().toString());
			if( issuingCertList.size() > 1){
				LOG.debug("more than one issuer found for ertificate id '{}' by matching issuer name '{}'");
			}
		}
/*
		if( issuingCertList.isEmpty()){			
			LOG.debug("AKI from issuer name, trying RDN matching");
			// @todo
		}
*/
		// no issuing certificate found 
		//  @todo
		// may not be a reason for a GeneralSecurityException
		if( issuingCertList.isEmpty()){			
			throw new GeneralSecurityException("no issuing certificate for '" + x509CertHolder.getSubject().toString() +"' in certificate store.");
		}

		// that's wierd!!
		if( issuingCertList.size() > 1){
			if( LOG.isDebugEnabled()) {
				LOG.debug("more than one issuer found ");
				for( Certificate issuer: issuingCertList) {
					LOG.debug("possible issuer id '{}' subject '{}'", issuer.getId(), issuer.getSubject());
				}
			}
			throw new GeneralSecurityException("more than one ("+issuingCertList.size()+") issuing certificate for '" + x509CertHolder.getSubject().toString() +"' in certificate store.");
		}

		Certificate issuerDao = issuingCertList.iterator().next();

		if( LOG.isDebugEnabled()) {
			LOG.debug("issuerDao has attributes: ");
			/*
			for( CertificateAttribute cad: issuerDao.getCertificateAttributes()){
				LOG.debug("Name '" + cad.getName() +"' got value '" + cad.getValue() + "'");
			}
			*/
		}

		return issuerDao;
	}

	/**
	 * 
	 * @param cert
	 * @return
	 * @throws GeneralSecurityException
	 */
	private Certificate findRootCertificate(Certificate cert) throws GeneralSecurityException {
		
		for(int i = 0; i < 10; i++) {
			
			// end of chain?
			if( cert.isSelfsigned()) {
				
				// hurra, terminated ...
				return cert;
			}
			
			// step up one level
			Certificate issuingCert = cert.getIssuingCertificate();
			
			// is the issuer already known?
			if( issuingCert == null) {
				
				// no, try to find it
				issuingCert = findIssuingCertificate(cert);
				if(issuingCert != null) {
					cert.setIssuingCertificate(issuingCert);
					certificateRepository.save(cert);
					LOG.debug("determined issuing certificate {} for {}", issuingCert.getId(), cert.getId());
				} else {
					break;
				}
			}
			
			cert = issuingCert;
		}

		LOG.info("unable to determined issuing certificate for {}", cert.getId());
		return null;
	}


	/**
	 * @param x509CertHolder
	 * @param aki
	 * @return
	 */
	private List<Certificate> findCertsByAKI(X509CertificateHolder x509CertHolder, AuthorityKeyIdentifier aki) {
		
		String aKIBase64 = Base64.encodeBase64String(aki.getKeyIdentifier());
		LOG.debug("looking for issuer of certificate '" + x509CertHolder.getSubject().toString() +"', issuer selected by its SKI '" + aKIBase64 + "'");
		List<Certificate> issuingCertList = certificateRepository.findByAttributeValue(CertificateAttribute.ATTRIBUTE_SKI, aKIBase64);
		if( issuingCertList.isEmpty()) {
			LOG.debug("no certificate found for AKI {}", aKIBase64);
		}
		return issuingCertList;
	}


	/**
	 * @return
	 * @throws GeneralSecurityException
	 *
	private X509ExtensionUtils getX509UtilInstance() throws GeneralSecurityException {
		DigestCalculator digCalc;
		try {
			digCalc = new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
		} catch (OperatorCreationException e) {
			LOG.warn("Problem instatiating digest calculator for SHA1", e);
			throw new GeneralSecurityException(e.getMessage());
		}
		X509ExtensionUtils x509ExtensionUtils = new X509ExtensionUtils(digCalc);
		return x509ExtensionUtils;
	}
*/
	/**
	 * @deprecated
	 * 
	 * @param csrBase64
	 * @param p10ReqHolder
	 * @param processInstanceId
	 * @return
	 */
	public CSR createCSR(final String csrBase64, final Pkcs10RequestHolder p10ReqHolder, final String processInstanceId) {

		CSR csr = new CSR();
		
		csr.setStatus(CsrStatus.PENDING);
		
		csr.setCsrBase64(csrBase64);

		csr.setSigningAlgorithm(p10ReqHolder.getSigningAlgorithm());

		csr.setIsCSRValid(p10ReqHolder.isCSRValid());

		csr.setx509KeySpec(p10ReqHolder.getX509KeySpec());

		csr.setPublicKeyAlgorithm(p10ReqHolder.getPublicKeyAlgorithm());

		csr.setPublicKeyHash(p10ReqHolder.getPublicKeyHash());
		
		csr.setSubjectPublicKeyInfoBase64(p10ReqHolder.getSubjectPublicKeyInfoBase64());

		/*
		 * if( p10ReqHolder.publicSigningKey != null ){ try {
		 * this.setPublicKeyPEM(cryptoUtil.publicKeyToPem(
		 * p10ReqHolder.publicSigningKey)); } catch (IOException e) {
		 * LOG.warn("wrapping of public key into PEM failed."); } }
		 */
		 csr.setProcessInstanceId(processInstanceId);
		 csr.setRequestedOn(Instant.now());

		LOG.debug("RDN arr #" + p10ReqHolder.getSubjectRDNs().length);

		Set<de.trustable.ca3s.core.domain.RDN> newRdns = new HashSet<de.trustable.ca3s.core.domain.RDN>();

		for (RDN currentRdn : p10ReqHolder.getSubjectRDNs()) {

			de.trustable.ca3s.core.domain.RDN rdnDao = new de.trustable.ca3s.core.domain.RDN();
			rdnDao.setCsr(csr);

			LOG.debug("AttributeTypeAndValue arr #" + currentRdn.size());
			Set<RDNAttribute> rdnAttributes = new HashSet<RDNAttribute>();

			AttributeTypeAndValue[] attrTVArr = currentRdn.getTypesAndValues();
			for (AttributeTypeAndValue attrTV : attrTVArr) {
				
				RDNAttribute rdnAtt = new RDNAttribute();
				rdnAtt.setRdn(rdnDao);
				rdnAtt.setAttributeType(attrTV.getType().toString());
				rdnAtt.setAttributeValue(attrTV.getValue().toString());
				LOG.debug("Adding RDNAttribute: '{}' = '{}'", attrTV.getType().toString(), attrTV.getValue().toString());
				
				rdnAttributes.add(rdnAtt);
			}

			rdnDao.setRdnAttributes(rdnAttributes);
			newRdns.add(rdnDao);
		}
		
		if(p10ReqHolder.getSubjectRDNs().length == 0) {

			LOG.info("Subject empty, using SANs" );
			Set<GeneralName> gNameSet = getSANList(p10ReqHolder);
			for( GeneralName gName : gNameSet) {
				if( GeneralName.dNSName == gName.getTagNo()) {
					
					de.trustable.ca3s.core.domain.RDN rdnDao = new de.trustable.ca3s.core.domain.RDN();
					rdnDao.setCsr(csr);
					
					Set<RDNAttribute> rdnAttributes = new HashSet<RDNAttribute>();
					RDNAttribute rdnAtt = new RDNAttribute();
					rdnAtt.setRdn(rdnDao);
					rdnAtt.setAttributeType(X509ObjectIdentifiers.commonName.toString());
					rdnAtt.setAttributeValue(gName.getName().toString());
					rdnAttributes.add(rdnAtt);
					rdnDao.setRdnAttributes(rdnAttributes);
					newRdns.add(rdnDao);
					LOG.info("First DNS SAN inserted as CN: " + gName.getName().toString() );
					break; // just one CN !
				}
			}
			
		}
		
		
		csr.setRdns(newRdns);

		Set<RequestAttribute> newRas = new HashSet<RequestAttribute>();

		for (Attribute attr : p10ReqHolder.getReqAttributes()) {

			RequestAttribute reqAttrs = new RequestAttribute();
			reqAttrs.setCsr(csr);
			reqAttrs.setAttributeType( attr.getAttrType().toString());

			Set<RequestAttributeValue> requestAttributes = new HashSet<RequestAttributeValue>();
			String type = attr.getAttrType().toString();
			ASN1Set valueSet = attr.getAttrValues();
			LOG.debug("AttributeSet type " + type + " #" + valueSet.size());

			for (ASN1Encodable asn1Enc : valueSet.toArray()) {
				String value = asn1Enc.toString();
				LOG.debug("Attribute value " + value);

				RequestAttributeValue reqAttr = new RequestAttributeValue();
				reqAttr.setReqAttr(reqAttrs);
				reqAttr.setAttributeValue(asn1Enc.toString());
				requestAttributes.add(reqAttr);
			}
			reqAttrs.setRequestAttributeValues(requestAttributes);
			newRas.add(reqAttrs);
		}
		csr.setRas(newRas);
		
		return csr;
	}

	public Set<GeneralName> getSANList(X509CertificateHolder x509CertHolder){
		
		Set<GeneralName> generalNameSet = new HashSet<GeneralName>();
		
		Extensions exts = x509CertHolder.getExtensions();
		for( ASN1ObjectIdentifier objId : exts.getExtensionOIDs()) {
			if( Extension.subjectAlternativeName.equals(objId)) {
				
				ASN1OctetString octString = exts.getExtension(objId).getExtnValue();
				GeneralNames names = GeneralNames.getInstance(octString);
				LOG.debug("Attribute value SAN" + names);
				LOG.debug("SAN values #" + names.getNames().length);
				
				for (GeneralName gnSAN : names.getNames()) {
					LOG.debug("GN " + gnSAN.toString());
					generalNameSet.add(gnSAN);
					
				}
			}
		}
		return generalNameSet;
	}

	public Set<GeneralName> getSANList(Pkcs10RequestHolder p10ReqHolder){
		
		Set<GeneralName> generalNameSet = new HashSet<GeneralName>();
		
		for( Attribute attr : p10ReqHolder.getReqAttributes()) {
			if( PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.equals(attr.getAttrType())){

				ASN1Set valueSet = attr.getAttrValues();
				LOG.debug("ExtensionRequest / AttrValues has {} elements", valueSet.size());
				for (ASN1Encodable asn1Enc : valueSet) {
					DERSequence derSeq = (DERSequence)asn1Enc;

					LOG.debug("ExtensionRequest / DERSequence has {} elements", derSeq.size());
					LOG.debug("ExtensionRequest / DERSequence[0] is a  {}", derSeq.getObjectAt(0).getClass().getName());

					DERSequence derSeq2 = (DERSequence)derSeq.getObjectAt(0);
					LOG.debug("ExtensionRequest / DERSequence2 has {} elements", derSeq2.size());
					LOG.debug("ExtensionRequest / DERSequence2[0] is a  {}", derSeq2.getObjectAt(0).getClass().getName());


					ASN1ObjectIdentifier objId = (ASN1ObjectIdentifier)(derSeq2.getObjectAt(0));
					if( Extension.subjectAlternativeName.equals(objId)) {
						DEROctetString derStr = (DEROctetString)derSeq2.getObjectAt(1);
						GeneralNames names = GeneralNames.getInstance(derStr.getOctets());
						LOG.debug("Attribute value SAN" + names);
						LOG.debug("SAN values #" + names.getNames().length);
						
						for (GeneralName gnSAN : names.getNames()) {
							LOG.debug("GN " + gnSAN.toString());
							generalNameSet.add(gnSAN);
							
						}
					} else {
						LOG.info("Unexpected Extensions Attribute value " + objId.getId());
					}
				}
				
			}
		}
		return generalNameSet;
	}

	/**
	 * 
	 * @param keyPair
	 * @return
	 * @throws IOException
*/	 
	public void storePrivateKey(Certificate cert, KeyPair keyPair) throws IOException {
		
		StringWriter sw = new StringWriter();
		PemObject pemObject = new PemObject( "PRIVATE KEY", keyPair.getPrivate() .getEncoded());
		PemWriter pemWriter = new PemWriter(sw);
		try {
			pemWriter.writeObject(pemObject);
		} finally {
			pemWriter.close();
		}

		LOG.debug("new private key as PEM : " + sw.toString());

		ProtectedContent pt = protUtil.createProtectedContent(sw.toString(), ProtectedContentType.KEY, ContentRelationType.CERTIFICATE, cert.getId());
		protContentRepository.save(pt);
	}

	/**
	 * 
	 * @param cert
	 * @return
	 */
    public PrivateKey getPrivateKey(Certificate cert) {
        
        PrivateKey priKey = null;
        
		try {
			List<ProtectedContent> pcList = protContentRepository.findByCertificateId(cert.getId());
			
			if( pcList.isEmpty()) {
	            LOG.error("retrieval of private key for certificate '{}' returns not key!", cert.getId());
			} else {
				if( pcList.size() > 1) {
		            LOG.warn("retrieval of private key for certificate '{}' returns more than one key ({}) !", cert.getId(), pcList.size());
				}
				
				String content = protUtil.unprotectString( pcList.get(0).getContentBase64());
				priKey = cryptoUtil.convertPemToPrivateKey (content);
		        LOG.debug("getPrivateKey() returns " + priKey.toString());
			}
			
		} catch (GeneralSecurityException e) {
            LOG.warn("getPrivateKey", e);
		}

        return priKey;
    }


}
