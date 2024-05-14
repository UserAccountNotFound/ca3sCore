/*
 * EJBCA REST Interface
 * API reference documentation.
 *
 * OpenAPI spec version: 1.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package de.trustable.ca3s.core.service.dto.ejbca;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import org.threeten.bp.OffsetDateTime;

import java.util.Objects;
/**
 * CaInfoRestResponse
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-02-27T11:17:53.164838525Z[GMT]")

public class CaInfoRestResponse {
  @SerializedName("id")
  private Integer id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("subject_dn")
  private String subjectDn = null;

  @SerializedName("issuer_dn")
  private String issuerDn = null;

  @SerializedName("expiration_date")
  private OffsetDateTime expirationDate = null;

  public CaInfoRestResponse id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * CA identifier
   * @return id
  **/
  @Schema(example = "12345678", description = "CA identifier")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public CaInfoRestResponse name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Certificate Authority (CA) name
   * @return name
  **/
  @Schema(example = "ExampleCA", description = "Certificate Authority (CA) name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CaInfoRestResponse subjectDn(String subjectDn) {
    this.subjectDn = subjectDn;
    return this;
  }

   /**
   * Subject Distinguished Name
   * @return subjectDn
  **/
  @Schema(example = "CN=ExampleCA,O=Sample,C=SE", description = "Subject Distinguished Name")
  public String getSubjectDn() {
    return subjectDn;
  }

  public void setSubjectDn(String subjectDn) {
    this.subjectDn = subjectDn;
  }

  public CaInfoRestResponse issuerDn(String issuerDn) {
    this.issuerDn = issuerDn;
    return this;
  }

   /**
   * Issuer Distinguished Name
   * @return issuerDn
  **/
  @Schema(example = "CN=ExampleCA,O=Sample,C=SE", description = "Issuer Distinguished Name")
  public String getIssuerDn() {
    return issuerDn;
  }

  public void setIssuerDn(String issuerDn) {
    this.issuerDn = issuerDn;
  }

  public CaInfoRestResponse expirationDate(OffsetDateTime expirationDate) {
    this.expirationDate = expirationDate;
    return this;
  }

   /**
   * Expiration date
   * @return expirationDate
  **/
  @Schema(example = "2038-01-19T03:14:07Z", description = "Expiration date")
  public OffsetDateTime getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(OffsetDateTime expirationDate) {
    this.expirationDate = expirationDate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CaInfoRestResponse caInfoRestResponse = (CaInfoRestResponse) o;
    return Objects.equals(this.id, caInfoRestResponse.id) &&
        Objects.equals(this.name, caInfoRestResponse.name) &&
        Objects.equals(this.subjectDn, caInfoRestResponse.subjectDn) &&
        Objects.equals(this.issuerDn, caInfoRestResponse.issuerDn) &&
        Objects.equals(this.expirationDate, caInfoRestResponse.expirationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, subjectDn, issuerDn, expirationDate);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CaInfoRestResponse {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subjectDn: ").append(toIndentedString(subjectDn)).append("\n");
    sb.append("    issuerDn: ").append(toIndentedString(issuerDn)).append("\n");
    sb.append("    expirationDate: ").append(toIndentedString(expirationDate)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
