package de.trustable.ca3s.core.web.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.CertificateService;
import de.trustable.ca3s.core.service.dto.CertificateView;

/**
 * REST controller for reading {@link de.trustable.ca3s.core.domain.Certificate} using the convenient CertificateView object.
 * Just read-only access to this resource.
 * 
 */
@RestController
@RequestMapping("/api")
public class CertificateViewResource {

    private final Logger log = LoggerFactory.getLogger(CertificateViewResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CertificateService certificateService;

    public CertificateViewResource(CertificateService certificateService) {
        this.certificateService = certificateService;
    }


    /**
     * {@code GET  /certificates} : get all the certificates.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificates in body.
     */
/*    
    @GetMapping("/certificateViews")
    public ResponseEntity<List<Certificate>> getAllCertificates(Pageable pageable) {
        log.debug("REST request to get a page of Certificates");
        Page<Certificate> page = certificateService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
*/
    
    /**
     * {@code GET  /certificates/:id} : get the "id" certificate.
     *
     * @param id the id of the certificate to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the certificate, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/certificateViews/{id}")
    public ResponseEntity<CertificateView> getCertificate(@PathVariable Long id) {
        log.debug("REST request to get CertificateView : {}", id);
        Optional<Certificate> certificateOpt = certificateService.findOne(id);
        
        if( certificateOpt.isPresent() ) {
    		return new ResponseEntity<CertificateView>(new CertificateView(certificateOpt.get()), HttpStatus.OK);
        }
        
		return ResponseEntity.notFound().build();
    }

}