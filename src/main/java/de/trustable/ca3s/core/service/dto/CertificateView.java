package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.web.rest.data.NamedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A certificate view from a given certificate and its attributes
 */
public class CertificateView implements Serializable {

    private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(CertificateView.class);

    private Long id;
    private Long csrId;
    private Long issuerId;

    private String tbsDigest;

    private String subject;
    private String rdn_c;
    private String rdn_cn;
    private String rdn_o;
    private String rdn_ou;
    private String rdn_s;
    private String rdn_l;

    private String sans;
    private String issuer;
    private String root;
    private String fingerprintSha1;
    private String fingerprintSha256;

    private String type;
    private String keyLength;
    private String keyAlgorithm;
    private String signingAlgorithm;
    private String paddingAlgorithm;
    private String hashAlgorithm;

    private String description;

    private String serial;
    private Instant validFrom;
    private Instant validTo;
    private Instant contentAddedAt;
    private Instant revokedSince;
    private String revocationReason;
    private Boolean revoked;

    private Boolean selfsigned;
    private Boolean ca;
    private Boolean intermediate;
    private Boolean endEntity;
    private Long chainLength;
    private String[] usage;
    private String[] extUsage;
    private String[] sanArr;
    private Long caConnectorId;
    private Long caProcessingId;
    private String processingCa;
    private Long acmeAccountId;
    private Long acmeOrderId;
    private Long scepTransId;
    private String scepRecipient;

    private String fileSource;
    private String uploadedBy;
    private String revokedBy;
    private String requestedBy;
    private String crlUrl;
    private Instant crlNextUpdate;

    private String certB64;
    private String downloadFilename;

	private Boolean isServersideKeyGeneration = false;

	private String comment;

    private Boolean isAuditPresent = false;

    private NamedValue[] arArr;

    public CertificateView() {}

    public CertificateView(final Certificate cert) {
    	this.id = cert.getId();
    	this.tbsDigest = cert.getTbsDigest();
    	this.subject = cert.getSubject();
    	this.sans = cert.getSans();
        this.issuer = cert.getIssuer();
    	this.type = cert.getType();
   		this.keyLength = cert.getKeyLength().toString();
   		this.keyAlgorithm = cert.getKeyAlgorithm();
		this.signingAlgorithm = cert.getSigningAlgorithm();
		this.paddingAlgorithm = cert.getPaddingAlgorithm();
		this.hashAlgorithm = cert.getHashingAlgorithm();
    	this.description = cert.getDescription();
    	this.serial = cert.getSerial();
    	this.validFrom = cert.getValidFrom();
    	this.validTo = cert.getValidTo();
    	this.contentAddedAt = cert.getContentAddedAt();
    	this.revokedSince = cert.getRevokedSince();
    	this.revocationReason = cert.getRevocationReason();
    	this.revoked = cert.isRevoked();
    	this.certB64 = cert.getContent();

    	if( cert.getCsr() != null) {
    		this.requestedBy = cert.getCsr().getRequestedBy();
    		this.csrId = cert.getCsr().getId();
    		this.isServersideKeyGeneration  = cert.getCsr().isServersideKeyGeneration();
    	} else {
    		this.requestedBy =  "";
    	}

    	if( cert.getIssuingCertificate() != null) {
    		this.issuerId = cert.getIssuingCertificate().getId();
    	}

    	List<String> usageList = new ArrayList<>();
        List<String> extUsageList = new ArrayList<>();
        List<String> sanList = new ArrayList<>();

    	for( CertificateAttribute certAttr: cert.getCertificateAttributes()) {
    		if( CertificateAttribute.ATTRIBUTE_CA_CONNECTOR_ID.equalsIgnoreCase(certAttr.getName())) {
    			this.caConnectorId = Long.parseLong(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID.equalsIgnoreCase(certAttr.getName())) {
    			this.caProcessingId = Long.parseLong(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_PROCESSING_CA.equalsIgnoreCase(certAttr.getName())) {
    			this.processingCa = certAttr.getValue();
    		} else if( CertificateAttribute.ATTRIBUTE_SOURCE.equalsIgnoreCase(certAttr.getName())) {
    			this.fileSource = certAttr.getValue();
    		} else if( CertificateAttribute.ATTRIBUTE_UPLOADED_BY.equalsIgnoreCase(certAttr.getName())) {
    			this.uploadedBy = certAttr.getValue();
    		} else if( CertificateAttribute.ATTRIBUTE_REVOKED_BY.equalsIgnoreCase(certAttr.getName())) {
    			this.revokedBy = certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_CRL_URL.equalsIgnoreCase(certAttr.getName())) {
                this.crlUrl= certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_FINGERPRINT_SHA1.equalsIgnoreCase(certAttr.getName())) {
                this.fingerprintSha1 = certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_FINGERPRINT_SHA256.equalsIgnoreCase(certAttr.getName())) {
                this.fingerprintSha256 = certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_RDN_CN.equalsIgnoreCase(certAttr.getName())) {
                this.rdn_cn = certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_RDN_C.equalsIgnoreCase(certAttr.getName())) {
                this.rdn_c = certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_RDN_O.equalsIgnoreCase(certAttr.getName())) {
                this.rdn_o = certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_RDN_OU.equalsIgnoreCase(certAttr.getName())) {
                this.rdn_ou = certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_RDN_L.equalsIgnoreCase(certAttr.getName())) {
                this.rdn_l = certAttr.getValue();
            } else if( CertificateAttribute.ATTRIBUTE_RDN_S.equalsIgnoreCase(certAttr.getName())) {
                this.rdn_s = certAttr.getValue();

            } else if( CertificateAttribute.ATTRIBUTE_CRL_NEXT_UPDATE.equalsIgnoreCase(certAttr.getName())) {
    			this.crlNextUpdate = Instant.ofEpochMilli(Long.parseLong(certAttr.getValue()));
    		} else if( CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID.equalsIgnoreCase(certAttr.getName())) {
    			this.acmeAccountId = Long.parseLong(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_ACME_ORDER_ID.equalsIgnoreCase(certAttr.getName())) {
    			this.acmeOrderId = Long.parseLong(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_SCEP_RECIPIENT.equalsIgnoreCase(certAttr.getName())) {
    			this.scepRecipient = certAttr.getValue();
    		} else if( CertificateAttribute.ATTRIBUTE_SCEP_TRANS_ID.equalsIgnoreCase(certAttr.getName())) {
    			this.scepTransId = Long.parseLong(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_SELFSIGNED.equalsIgnoreCase(certAttr.getName())) {
    			this.selfsigned = Boolean.valueOf(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_CA.equalsIgnoreCase(certAttr.getName())) {
    			this.ca = Boolean.valueOf(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_CA3S_ROOT.equalsIgnoreCase(certAttr.getName())) {
    		} else if( CertificateAttribute.ATTRIBUTE_ROOT.equalsIgnoreCase(certAttr.getName())) {
    		} else if( CertificateAttribute.ATTRIBUTE_CA3S_INTERMEDIATE.equalsIgnoreCase(certAttr.getName())) {
    		} else if( CertificateAttribute.ATTRIBUTE_END_ENTITY.equalsIgnoreCase(certAttr.getName())) {
    			this.endEntity = Boolean.valueOf(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_CHAIN_LENGTH.equalsIgnoreCase(certAttr.getName())) {
    			this.chainLength = Long.parseLong(certAttr.getValue());
    		} else if( CertificateAttribute.ATTRIBUTE_USAGE.equalsIgnoreCase(certAttr.getName())) {
    			usageList.add(certAttr.getValue());
            } else if(CertificateAttribute.ATTRIBUTE_EXTENDED_USAGE.equalsIgnoreCase(certAttr.getName())) {
                extUsageList.add(certAttr.getValue());
            } else if(CertificateAttribute.ATTRIBUTE_SAN.equalsIgnoreCase(certAttr.getName())) {
                sanList.add(certAttr.getValue());
            } else if(CertificateAttribute.ATTRIBUTE_COMMENT.equalsIgnoreCase(certAttr.getName())) {
                this.setComment(certAttr.getValue());
    		}else {
    			LOG.debug("Irrelevant certificate attribute '{}' with value '{}'", certAttr.getName(), certAttr.getValue());

    		}
    	}
    	this.usage = usageList.toArray(new String[0]);
        this.extUsage = extUsageList.toArray(new String[0]);
        this.sanArr = sanList.toArray(new String[0]);

    	this.downloadFilename = CertificateUtil.getDownloadFilename(cert);

    	this.arArr = copyArAttributes(cert);

    }

	public Long getId() {
		return id;
	}

	public String getTbsDigest() {
		return tbsDigest;
	}

	public String getSubject() {
		return subject;
	}

	public String getIssuer() {
		return issuer;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public String getSerial() {
		return serial;
	}

	public Instant getValidFrom() {
		return validFrom;
	}

	public Instant getValidTo() {
		return validTo;
	}

	public Instant getContentAddedAt() {
		return contentAddedAt;
	}

	public Instant getRevokedSince() {
		return revokedSince;
	}

	public String getRevocationReason() {
		return revocationReason;
	}

	public Boolean getRevoked() {
		return revoked;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTbsDigest(String tbsDigest) {
		this.tbsDigest = tbsDigest;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public void setValidFrom(Instant validFrom) {
		this.validFrom = validFrom;
	}

	public void setValidTo(Instant validTo) {
		this.validTo = validTo;
	}

	public void setContentAddedAt(Instant contentAddedAt) {
		this.contentAddedAt = contentAddedAt;
	}

	public void setRevokedSince(Instant revokedSince) {
		this.revokedSince = revokedSince;
	}

	public void setRevocationReason(String revocationReason) {
		this.revocationReason = revocationReason;
	}

	public void setRevoked(Boolean revoked) {
		this.revoked = revoked;
	}

	public String getKeyLength() {
		return keyLength;
	}

	public String getSigningAlgorithm() {
		return signingAlgorithm;
	}

	public String getPaddingAlgorithm() {
		return paddingAlgorithm;
	}

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public void setKeyLength(String keyLength) {
		this.keyLength = keyLength;
	}

	public void setSigningAlgorithm(String signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
	}

	public void setPaddingAlgorithm(String paddingAlgorithm) {
		this.paddingAlgorithm = paddingAlgorithm;
	}

	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	public String getSans() {
		return sans;
	}

	public void setSans(String sans) {
		this.sans = sans;
	}

	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(String keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public Boolean getSelfsigned() {
		return selfsigned;
	}

	public Boolean getCa() {
		return ca;
	}

	public String getRoot() {
		return root;
	}

	public Boolean getIntermediate() {
		return intermediate;
	}

	public Boolean getEndEntity() {
		return endEntity;
	}

	public Long getChainLength() {
		return chainLength;
	}

	public String[] getUsage() {
		return usage;
	}

	public String[] getSanArr() {
		return sanArr;
	}

	public Long getCaConnectorId() {
		return caConnectorId;
	}

	public Long getCaProcessingId() {
		return caProcessingId;
	}

	public String getFileSource() {
		return fileSource;
	}

	public String getUploadedBy() {
		return uploadedBy;
	}

	public String getRevokedBy() {
		return revokedBy;
	}

	public String getCrlUrl() {
		return crlUrl;
	}

	public Instant getCrlNextUpdate() {
		return crlNextUpdate;
	}

	public void setSelfsigned(Boolean selfsigned) {
		this.selfsigned = selfsigned;
	}

	public void setCa(Boolean ca) {
		this.ca = ca;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public void setIntermediate(Boolean intermediate) {
		this.intermediate = intermediate;
	}

	public void setEndEntity(Boolean endEntity) {
		this.endEntity = endEntity;
	}

	public void setChainLength(Long chainLength) {
		this.chainLength = chainLength;
	}

	public void setUsage(String[] usage) {
		this.usage = usage;
	}

	public void setSanArr(String[] sanArr) {
		this.sanArr = sanArr;
	}

	public void setCaConnectorId(Long caConnectorId) {
		this.caConnectorId = caConnectorId;
	}

	public void setCaProcessingId(Long caProcessingId) {
		this.caProcessingId = caProcessingId;
	}

	public void setFileSource(String fileSource) {
		this.fileSource = fileSource;
	}

	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public void setRevokedBy(String revokedBy) {
		this.revokedBy = revokedBy;
	}

	public void setCrlUrl(String crlUrl) {
		this.crlUrl = crlUrl;
	}

	public void setCrlNextUpdate(Instant crlNextUpdate) {
		this.crlNextUpdate = crlNextUpdate;
	}

	public Long getAcmeAccountId() {
		return acmeAccountId;
	}

	public Long getAcmeOrderId() {
		return acmeOrderId;
	}

	public Long getScepTransId() {
		return scepTransId;
	}

	public String getScepRecipient() {
		return scepRecipient;
	}

	public void setAcmeAccountId(Long acmeAccountId) {
		this.acmeAccountId = acmeAccountId;
	}

	public void setAcmeOrderId(Long acmeOrderId) {
		this.acmeOrderId = acmeOrderId;
	}

	public void setScepTransId(Long scepTransId) {
		this.scepTransId = scepTransId;
	}

	public void setScepRecipient(String scepRecipient) {
		this.scepRecipient = scepRecipient;
	}

	public String[] getExtUsage() {
		return extUsage;
	}

	public void setExtUsage(String[] extUsage) {
		this.extUsage = extUsage;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public String getDownloadFilename() {
		return downloadFilename;
	}

	public void setDownloadFilename(String downloadFilename) {
		this.downloadFilename = downloadFilename;
	}

	public Long getCsrId() {
		return csrId;
	}

	public Long getIssuerId() {
		return issuerId;
	}

	public void setCsrId(Long csrId) {
		this.csrId = csrId;
	}

	public void setIssuerId(Long issuerId) {
		this.issuerId = issuerId;
	}

    public String getFingerprintSha1() {
        return fingerprintSha1;
    }

    public void setFingerprintSha1(String fingerprint) {
        this.fingerprintSha1 = fingerprint;
    }

    public String getFingerprintSha256() {
        return fingerprintSha256;
    }

    public void setFingerprintSha256(String fingerprint) {
        this.fingerprintSha256 = fingerprint;
    }

    public String getProcessingCa() {
		return processingCa;
	}

	public void setProcessingCa(String processingCa) {
		this.processingCa = processingCa;
	}

	public String getCertB64() {
		return certB64;
	}

	public void setCertB64(String certB64) {
		this.certB64 = certB64;
	}

	public Boolean getIsServersideKeyGeneration() {
		return isServersideKeyGeneration;
	}

	public void setIsServersideKeyGeneration(Boolean isServersideKeyGeneration) {
		this.isServersideKeyGeneration = isServersideKeyGeneration;
	}

    public String getRdn_c() {
        return rdn_c;
    }

    public void setRdn_c(String rdn_c) {
        this.rdn_c = rdn_c;
    }

    public String getRdn_cn() {
        return rdn_cn;
    }

    public void setRdn_cn(String rdn_cn) {
        this.rdn_cn = rdn_cn;
    }

    public String getRdn_o() {
        return rdn_o;
    }

    public void setRdn_o(String rdn_o) {
        this.rdn_o = rdn_o;
    }

    public String getRdn_ou() {
        return rdn_ou;
    }

    public void setRdn_ou(String rdn_ou) {
        this.rdn_ou = rdn_ou;
    }

    public String getRdn_s() {
        return rdn_s;
    }

    public void setRdn_s(String rdn_s) {
        this.rdn_s = rdn_s;
    }

    public String getRdn_l() {
        return rdn_l;
    }

    public void setRdn_l(String rdn_l) {
        this.rdn_l = rdn_l;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public NamedValue[] getArArr() {
        return arArr;
    }

    public void setArArr(NamedValue[] arArr) {
        this.arArr = arArr;
    }

    private NamedValue[] copyArAttributes(final Certificate cert) {

        List<NamedValue> nvList = new ArrayList<>();
        for(CertificateAttribute certAttr: cert.getCertificateAttributes()){
            if(certAttr.getName().startsWith(CsrAttribute.ARA_PREFIX) ){
                NamedValue nv = new NamedValue();
                nv.setName(certAttr.getName().substring(CsrAttribute.ARA_PREFIX.length()));
                nv.setValue(certAttr.getValue());
                nvList.add(nv);
            }
        }
        return nvList.toArray(new NamedValue[0]);
    }

    public Boolean getAuditPresent() {
        return isAuditPresent;
    }

    public void setAuditPresent(Boolean auditPresent) {
        isAuditPresent = auditPresent;
    }
}
