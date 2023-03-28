package de.trustable.ca3s.core.domain;

import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.Interval;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CAConnectorConfig.
 */
@Entity
@Table(name = "ca_connector_config")
@NamedQueries({
	@NamedQuery(name = "CAConnectorConfig.findAllCertGenerators",
	query = "SELECT ccc FROM CAConnectorConfig ccc WHERE " +
			"ccc.caConnectorType in ( 'ADCS', 'CMP', 'INTERNAL' )"
    ),
	@NamedQuery(name = "CAConnectorConfig.findbyName",
	query = "SELECT ccc FROM CAConnectorConfig ccc WHERE " +
			"ccc.name = :name"
    )
})
public class CAConnectorConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ca_connector_type", nullable = false)
    private CAConnectorType caConnectorType;

    @Column(name = "ca_url")
    private String caUrl;

    @Column(name = "polling_offset")
    private Integer pollingOffset;

    @Column(name = "default_ca")
    private Boolean defaultCA;

    @Column(name = "trust_selfsigned_certificates")
    private Boolean trustSelfsignedCertificates;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "selector")
    private String selector;

    @Enumerated(EnumType.STRING)
    @Column(name = "jhi_interval")
    private Interval interval;

    @Column(name = "plain_secret")
    private String plainSecret;

    @OneToOne
    @JoinColumn(unique = true)
    private ProtectedContent secret;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CAConnectorConfig id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public CAConnectorConfig name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CAConnectorType getCaConnectorType() {
        return this.caConnectorType;
    }

    public CAConnectorConfig caConnectorType(CAConnectorType caConnectorType) {
        this.setCaConnectorType(caConnectorType);
        return this;
    }

    public void setCaConnectorType(CAConnectorType caConnectorType) {
        this.caConnectorType = caConnectorType;
    }

    public String getCaUrl() {
        return this.caUrl;
    }

    public CAConnectorConfig caUrl(String caUrl) {
        this.setCaUrl(caUrl);
        return this;
    }

    public void setCaUrl(String caUrl) {
        this.caUrl = caUrl;
    }

    public Integer getPollingOffset() {
        return this.pollingOffset;
    }

    public CAConnectorConfig pollingOffset(Integer pollingOffset) {
        this.setPollingOffset(pollingOffset);
        return this;
    }

    public void setPollingOffset(Integer pollingOffset) {
        this.pollingOffset = pollingOffset;
    }

    public Boolean isDefaultCA() {
        return defaultCA;
    }

    public Boolean getDefaultCA() {
        return this.defaultCA;
    }

    public CAConnectorConfig defaultCA(Boolean defaultCA) {
        this.setDefaultCA(defaultCA);
        return this;
    }

    public void setDefaultCA(Boolean defaultCA) {
        this.defaultCA = defaultCA;
    }

    public Boolean isActive() {
        return active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public CAConnectorConfig active(Boolean active) {
        this.active = active;
        return this;
    }

    public Boolean getTrustSelfsignedCertificates() {
        return trustSelfsignedCertificates;
    }

    public void setTrustSelfsignedCertificates(Boolean trustSelfsignedCertificates) {
        this.trustSelfsignedCertificates = trustSelfsignedCertificates;
    }

    public CAConnectorConfig trustSelfsignedCertificates(Boolean trustSelfsignedCertificates) {
        this.trustSelfsignedCertificates = trustSelfsignedCertificates;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getSelector() {
        return this.selector;
    }

    public CAConnectorConfig selector(String selector) {
        this.setSelector(selector);
        return this;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Interval getInterval() {
        return this.interval;
    }

    public CAConnectorConfig interval(Interval interval) {
        this.setInterval(interval);
        return this;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public String getPlainSecret() {
        return this.plainSecret;
    }

    public CAConnectorConfig plainSecret(String plainSecret) {
        this.setPlainSecret(plainSecret);
        return this;
    }

    public void setPlainSecret(String plainSecret) {
        this.plainSecret = plainSecret;
    }

    public ProtectedContent getSecret() {
        return this.secret;
    }

    public void setSecret(ProtectedContent protectedContent) {
        this.secret = protectedContent;
    }

    public CAConnectorConfig secret(ProtectedContent protectedContent) {
        this.setSecret(protectedContent);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CAConnectorConfig)) {
            return false;
        }
        return id != null && id.equals(((CAConnectorConfig) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CAConnectorConfig{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", caConnectorType='" + getCaConnectorType() + "'" +
            ", caUrl='" + getCaUrl() + "'" +
            ", pollingOffset=" + getPollingOffset() +
            ", defaultCA='" + getDefaultCA() + "'" +
            ", active='" + getActive() + "'" +
            ", selector='" + getSelector() + "'" +
            ", interval='" + getInterval() + "'" +
            ", plainSecret='" + getPlainSecret() + "'" +
            "}";
    }
}
