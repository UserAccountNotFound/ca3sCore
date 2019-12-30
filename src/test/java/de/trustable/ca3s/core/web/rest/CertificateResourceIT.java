package de.trustable.ca3s.core.web.rest;

import static de.trustable.ca3s.core.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import de.trustable.ca3s.core.Ca3SJhApp;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.CertificateService;
import de.trustable.ca3s.core.web.rest.errors.ExceptionTranslator;

/**
 * Integration tests for the {@link CertificateResource} REST controller.
 */
@SpringBootTest(classes = Ca3SJhApp.class)
public class CertificateResourceIT {

    private static final String DEFAULT_TBS_DIGEST = "AAAAAAAAAA";
    private static final String UPDATED_TBS_DIGEST = "BBBBBBBBBB";

    private static final String DEFAULT_SUBJECT = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT = "BBBBBBBBBB";

    private static final String DEFAULT_ISSUER = "AAAAAAAAAA";
    private static final String UPDATED_ISSUER = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_SUBJECT_KEY_IDENTIFIER = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT_KEY_IDENTIFIER = "BBBBBBBBBB";

    private static final String DEFAULT_AUTHORITY_KEY_IDENTIFIER = "AAAAAAAAAA";
    private static final String UPDATED_AUTHORITY_KEY_IDENTIFIER = "BBBBBBBBBB";

    private static final String DEFAULT_FINGERPRINT = "AAAAAAAAAA";
    private static final String UPDATED_FINGERPRINT = "BBBBBBBBBB";

    private static final String DEFAULT_SERIAL = "AAAAAAAAAA";
    private static final String UPDATED_SERIAL = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_VALID_FROM = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VALID_FROM = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_VALID_FROM = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_VALID_TO = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VALID_TO = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_VALID_TO = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_CREATION_EXECUTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_CREATION_EXECUTION_ID = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_CONTENT_ADDED_AT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CONTENT_ADDED_AT = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_CONTENT_ADDED_AT = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_REVOKED_SINCE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_REVOKED_SINCE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_REVOKED_SINCE = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_REVOCATION_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REVOCATION_REASON = "BBBBBBBBBB";

    private static final Boolean DEFAULT_REVOKED = false;
    private static final Boolean UPDATED_REVOKED = true;

    private static final String DEFAULT_REVOCATION_EXECUTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_REVOCATION_EXECUTION_ID = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restCertificateMockMvc;

    private Certificate certificate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CertificateResource certificateResource = new CertificateResource(certificateService);
        this.restCertificateMockMvc = MockMvcBuilders.standaloneSetup(certificateResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Certificate createEntity(EntityManager em) {
        Certificate certificate = new Certificate()
            .tbsDigest(DEFAULT_TBS_DIGEST)
            .subject(DEFAULT_SUBJECT)
            .issuer(DEFAULT_ISSUER)
            .type(DEFAULT_TYPE)
            .description(DEFAULT_DESCRIPTION)
            .subjectKeyIdentifier(DEFAULT_SUBJECT_KEY_IDENTIFIER)
            .authorityKeyIdentifier(DEFAULT_AUTHORITY_KEY_IDENTIFIER)
            .fingerprint(DEFAULT_FINGERPRINT)
            .serial(DEFAULT_SERIAL)
            .validFrom(DEFAULT_VALID_FROM)
            .validTo(DEFAULT_VALID_TO)
            .creationExecutionId(DEFAULT_CREATION_EXECUTION_ID)
            .contentAddedAt(DEFAULT_CONTENT_ADDED_AT)
            .revokedSince(DEFAULT_REVOKED_SINCE)
            .revocationReason(DEFAULT_REVOCATION_REASON)
            .revoked(DEFAULT_REVOKED)
            .revocationExecutionId(DEFAULT_REVOCATION_EXECUTION_ID)
            .content(DEFAULT_CONTENT);
        return certificate;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Certificate createUpdatedEntity(EntityManager em) {
        Certificate certificate = new Certificate()
            .tbsDigest(UPDATED_TBS_DIGEST)
            .subject(UPDATED_SUBJECT)
            .issuer(UPDATED_ISSUER)
            .type(UPDATED_TYPE)
            .description(UPDATED_DESCRIPTION)
            .subjectKeyIdentifier(UPDATED_SUBJECT_KEY_IDENTIFIER)
            .authorityKeyIdentifier(UPDATED_AUTHORITY_KEY_IDENTIFIER)
            .fingerprint(UPDATED_FINGERPRINT)
            .serial(UPDATED_SERIAL)
            .validFrom(UPDATED_VALID_FROM)
            .validTo(UPDATED_VALID_TO)
            .creationExecutionId(UPDATED_CREATION_EXECUTION_ID)
            .contentAddedAt(UPDATED_CONTENT_ADDED_AT)
            .revokedSince(UPDATED_REVOKED_SINCE)
            .revocationReason(UPDATED_REVOCATION_REASON)
            .revoked(UPDATED_REVOKED)
            .revocationExecutionId(UPDATED_REVOCATION_EXECUTION_ID)
            .content(UPDATED_CONTENT);
        return certificate;
    }

    @BeforeEach
    public void initTest() {
        certificate = createEntity(em);
    }

    @Test
    @Transactional
    public void createCertificate() throws Exception {
        int databaseSizeBeforeCreate = certificateRepository.findAll().size();

        // Create the Certificate
        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isCreated());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeCreate + 1);
        Certificate testCertificate = certificateList.get(certificateList.size() - 1);
        assertThat(testCertificate.getTbsDigest()).isEqualTo(DEFAULT_TBS_DIGEST);
        assertThat(testCertificate.getSubject()).isEqualTo(DEFAULT_SUBJECT);
        assertThat(testCertificate.getIssuer()).isEqualTo(DEFAULT_ISSUER);
        assertThat(testCertificate.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testCertificate.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testCertificate.getSubjectKeyIdentifier()).isEqualTo(DEFAULT_SUBJECT_KEY_IDENTIFIER);
        assertThat(testCertificate.getAuthorityKeyIdentifier()).isEqualTo(DEFAULT_AUTHORITY_KEY_IDENTIFIER);
        assertThat(testCertificate.getFingerprint()).isEqualTo(DEFAULT_FINGERPRINT);
        assertThat(testCertificate.getSerial()).isEqualTo(DEFAULT_SERIAL);
        assertThat(testCertificate.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testCertificate.getValidTo()).isEqualTo(DEFAULT_VALID_TO);
        assertThat(testCertificate.getCreationExecutionId()).isEqualTo(DEFAULT_CREATION_EXECUTION_ID);
        assertThat(testCertificate.getContentAddedAt()).isEqualTo(DEFAULT_CONTENT_ADDED_AT);
        assertThat(testCertificate.getRevokedSince()).isEqualTo(DEFAULT_REVOKED_SINCE);
        assertThat(testCertificate.getRevocationReason()).isEqualTo(DEFAULT_REVOCATION_REASON);
        assertThat(testCertificate.isRevoked()).isEqualTo(DEFAULT_REVOKED);
        assertThat(testCertificate.getRevocationExecutionId()).isEqualTo(DEFAULT_REVOCATION_EXECUTION_ID);
        assertThat(testCertificate.getContent()).isEqualTo(DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    public void createCertificateWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = certificateRepository.findAll().size();

        // Create the Certificate with an existing ID
        certificate.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkTbsDigestIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setTbsDigest(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSubjectIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setSubject(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkIssuerIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setIssuer(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setType(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSerialIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setSerial(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValidFromIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setValidFrom(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValidToIsRequired() throws Exception {
        int databaseSizeBeforeTest = certificateRepository.findAll().size();
        // set the field null
        certificate.setValidTo(null);

        // Create the Certificate, which fails.

        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCertificates() throws Exception {
        // Initialize the database
        certificateRepository.saveAndFlush(certificate);

        // Get all the certificateList
        restCertificateMockMvc.perform(get("/api/certificates?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(certificate.getId().intValue())))
            .andExpect(jsonPath("$.[*].tbsDigest").value(hasItem(DEFAULT_TBS_DIGEST.toString())))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(DEFAULT_SUBJECT.toString())))
            .andExpect(jsonPath("$.[*].issuer").value(hasItem(DEFAULT_ISSUER.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].subjectKeyIdentifier").value(hasItem(DEFAULT_SUBJECT_KEY_IDENTIFIER.toString())))
            .andExpect(jsonPath("$.[*].authorityKeyIdentifier").value(hasItem(DEFAULT_AUTHORITY_KEY_IDENTIFIER.toString())))
            .andExpect(jsonPath("$.[*].fingerprint").value(hasItem(DEFAULT_FINGERPRINT.toString())))
            .andExpect(jsonPath("$.[*].serial").value(hasItem(DEFAULT_SERIAL.toString())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].creationExecutionId").value(hasItem(DEFAULT_CREATION_EXECUTION_ID.toString())))
            .andExpect(jsonPath("$.[*].contentAddedAt").value(hasItem(DEFAULT_CONTENT_ADDED_AT.toString())))
            .andExpect(jsonPath("$.[*].revokedSince").value(hasItem(DEFAULT_REVOKED_SINCE.toString())))
            .andExpect(jsonPath("$.[*].revocationReason").value(hasItem(DEFAULT_REVOCATION_REASON.toString())))
            .andExpect(jsonPath("$.[*].revoked").value(hasItem(DEFAULT_REVOKED.booleanValue())))
            .andExpect(jsonPath("$.[*].revocationExecutionId").value(hasItem(DEFAULT_REVOCATION_EXECUTION_ID.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())));
    }
    
    @Test
    @Transactional
    public void getCertificate() throws Exception {
        // Initialize the database
        certificateRepository.saveAndFlush(certificate);

        // Get the certificate
        restCertificateMockMvc.perform(get("/api/certificates/{id}", certificate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(certificate.getId().intValue()))
            .andExpect(jsonPath("$.tbsDigest").value(DEFAULT_TBS_DIGEST.toString()))
            .andExpect(jsonPath("$.subject").value(DEFAULT_SUBJECT.toString()))
            .andExpect(jsonPath("$.issuer").value(DEFAULT_ISSUER.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.subjectKeyIdentifier").value(DEFAULT_SUBJECT_KEY_IDENTIFIER.toString()))
            .andExpect(jsonPath("$.authorityKeyIdentifier").value(DEFAULT_AUTHORITY_KEY_IDENTIFIER.toString()))
            .andExpect(jsonPath("$.fingerprint").value(DEFAULT_FINGERPRINT.toString()))
            .andExpect(jsonPath("$.serial").value(DEFAULT_SERIAL.toString()))
            .andExpect(jsonPath("$.validFrom").value(DEFAULT_VALID_FROM.toString()))
            .andExpect(jsonPath("$.validTo").value(DEFAULT_VALID_TO.toString()))
            .andExpect(jsonPath("$.creationExecutionId").value(DEFAULT_CREATION_EXECUTION_ID.toString()))
            .andExpect(jsonPath("$.contentAddedAt").value(DEFAULT_CONTENT_ADDED_AT.toString()))
            .andExpect(jsonPath("$.revokedSince").value(DEFAULT_REVOKED_SINCE.toString()))
            .andExpect(jsonPath("$.revocationReason").value(DEFAULT_REVOCATION_REASON.toString()))
            .andExpect(jsonPath("$.revoked").value(DEFAULT_REVOKED.booleanValue()))
            .andExpect(jsonPath("$.revocationExecutionId").value(DEFAULT_REVOCATION_EXECUTION_ID.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCertificate() throws Exception {
        // Get the certificate
        restCertificateMockMvc.perform(get("/api/certificates/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCertificate() throws Exception {
        // Initialize the database
        certificateService.save(certificate);

        int databaseSizeBeforeUpdate = certificateRepository.findAll().size();

        // Update the certificate
        Certificate updatedCertificate = certificateRepository.findById(certificate.getId()).get();
        // Disconnect from session so that the updates on updatedCertificate are not directly saved in db
        em.detach(updatedCertificate);
        updatedCertificate
            .tbsDigest(UPDATED_TBS_DIGEST)
            .subject(UPDATED_SUBJECT)
            .issuer(UPDATED_ISSUER)
            .type(UPDATED_TYPE)
            .description(UPDATED_DESCRIPTION)
            .subjectKeyIdentifier(UPDATED_SUBJECT_KEY_IDENTIFIER)
            .authorityKeyIdentifier(UPDATED_AUTHORITY_KEY_IDENTIFIER)
            .fingerprint(UPDATED_FINGERPRINT)
            .serial(UPDATED_SERIAL)
            .validFrom(UPDATED_VALID_FROM)
            .validTo(UPDATED_VALID_TO)
            .creationExecutionId(UPDATED_CREATION_EXECUTION_ID)
            .contentAddedAt(UPDATED_CONTENT_ADDED_AT)
            .revokedSince(UPDATED_REVOKED_SINCE)
            .revocationReason(UPDATED_REVOCATION_REASON)
            .revoked(UPDATED_REVOKED)
            .revocationExecutionId(UPDATED_REVOCATION_EXECUTION_ID)
            .content(UPDATED_CONTENT);

        restCertificateMockMvc.perform(put("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCertificate)))
            .andExpect(status().isOk());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeUpdate);
        Certificate testCertificate = certificateList.get(certificateList.size() - 1);
        assertThat(testCertificate.getTbsDigest()).isEqualTo(UPDATED_TBS_DIGEST);
        assertThat(testCertificate.getSubject()).isEqualTo(UPDATED_SUBJECT);
        assertThat(testCertificate.getIssuer()).isEqualTo(UPDATED_ISSUER);
        assertThat(testCertificate.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testCertificate.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testCertificate.getSubjectKeyIdentifier()).isEqualTo(UPDATED_SUBJECT_KEY_IDENTIFIER);
        assertThat(testCertificate.getAuthorityKeyIdentifier()).isEqualTo(UPDATED_AUTHORITY_KEY_IDENTIFIER);
        assertThat(testCertificate.getFingerprint()).isEqualTo(UPDATED_FINGERPRINT);
        assertThat(testCertificate.getSerial()).isEqualTo(UPDATED_SERIAL);
        assertThat(testCertificate.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testCertificate.getValidTo()).isEqualTo(UPDATED_VALID_TO);
        assertThat(testCertificate.getCreationExecutionId()).isEqualTo(UPDATED_CREATION_EXECUTION_ID);
        assertThat(testCertificate.getContentAddedAt()).isEqualTo(UPDATED_CONTENT_ADDED_AT);
        assertThat(testCertificate.getRevokedSince()).isEqualTo(UPDATED_REVOKED_SINCE);
        assertThat(testCertificate.getRevocationReason()).isEqualTo(UPDATED_REVOCATION_REASON);
        assertThat(testCertificate.isRevoked()).isEqualTo(UPDATED_REVOKED);
        assertThat(testCertificate.getRevocationExecutionId()).isEqualTo(UPDATED_REVOCATION_EXECUTION_ID);
        assertThat(testCertificate.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void updateNonExistingCertificate() throws Exception {
        int databaseSizeBeforeUpdate = certificateRepository.findAll().size();

        // Create the Certificate

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCertificateMockMvc.perform(put("/api/certificates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCertificate() throws Exception {
        // Initialize the database
        certificateService.save(certificate);

        int databaseSizeBeforeDelete = certificateRepository.findAll().size();

        // Delete the certificate
        restCertificateMockMvc.perform(delete("/api/certificates/{id}", certificate.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Certificate.class);
        Certificate certificate1 = new Certificate();
        certificate1.setId(1L);
        Certificate certificate2 = new Certificate();
        certificate2.setId(certificate1.getId());
        assertThat(certificate1).isEqualTo(certificate2);
        certificate2.setId(2L);
        assertThat(certificate1).isNotEqualTo(certificate2);
        certificate1.setId(null);
        assertThat(certificate1).isNotEqualTo(certificate2);
    }
}
