package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * A AuditTrace.
 */
@Entity
@Table(name = "audit_trace")
public class AuditTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @NotNull
    @Column(name = "actor_role", nullable = false)
    private String actorRole;

    @NotNull
    @Column(name = "plain_content", nullable = false)
    private String plainContent;

    @NotNull
    @Column(name = "content_template", nullable = false)
    private String contentTemplate;

    @NotNull
    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private CSR csr;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private Certificate certificate;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private Pipeline pipeline;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private CAConnectorConfig caConnector;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private BPMNProcessInfo processInfo;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActorName() {
        return actorName;
    }

    public AuditTrace actorName(String actorName) {
        this.actorName = actorName;
        return this;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public AuditTrace actorRole(String actorRole) {
        this.actorRole = actorRole;
        return this;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getPlainContent() {
        return plainContent;
    }

    public AuditTrace plainContent(String plainContent) {
        this.plainContent = plainContent;
        return this;
    }

    public void setPlainContent(String plainContent) {
        this.plainContent = plainContent;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public AuditTrace contentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
        return this;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public AuditTrace createdOn(Instant createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public CSR getCsr() {
        return csr;
    }

    public AuditTrace csr(CSR cSR) {
        this.csr = cSR;
        return this;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public AuditTrace certificate(Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public AuditTrace pipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public CAConnectorConfig getCaConnector() {
        return caConnector;
    }

    public AuditTrace caConnector(CAConnectorConfig cAConnectorConfig) {
        this.caConnector = cAConnectorConfig;
        return this;
    }

    public void setCaConnector(CAConnectorConfig cAConnectorConfig) {
        this.caConnector = cAConnectorConfig;
    }

    public BPMNProcessInfo getProcessInfo() {
        return processInfo;
    }

    public AuditTrace processInfo(BPMNProcessInfo bPMNProcessInfo) {
        this.processInfo = bPMNProcessInfo;
        return this;
    }

    public void setProcessInfo(BPMNProcessInfo bPMNProcessInfo) {
        this.processInfo = bPMNProcessInfo;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditTrace)) {
            return false;
        }
        return id != null && id.equals(((AuditTrace) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditTrace{" +
            "id=" + getId() +
            ", actorName='" + getActorName() + "'" +
            ", actorRole='" + getActorRole() + "'" +
            ", plainContent='" + getPlainContent() + "'" +
            ", contentTemplate='" + getContentTemplate() + "'" +
            ", createdOn='" + getCreatedOn() + "'" +
            "}";
    }
}