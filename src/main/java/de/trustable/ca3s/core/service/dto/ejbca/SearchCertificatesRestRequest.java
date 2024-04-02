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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * SearchCertificatesRestRequest
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-02-27T11:17:53.164838525Z[GMT]")

public class SearchCertificatesRestRequest {
  @SerializedName("max_number_of_results")
  private Integer maxNumberOfResults = null;

  @SerializedName("criteria")
  private List<SearchCertificateCriteriaRestRequest> criteria = null;

  public SearchCertificatesRestRequest maxNumberOfResults(Integer maxNumberOfResults) {
    this.maxNumberOfResults = maxNumberOfResults;
    return this;
  }

   /**
   * Maximum number of results
   * @return maxNumberOfResults
  **/
  @Schema(example = "10", description = "Maximum number of results")
  public Integer getMaxNumberOfResults() {
    return maxNumberOfResults;
  }

  public void setMaxNumberOfResults(Integer maxNumberOfResults) {
    this.maxNumberOfResults = maxNumberOfResults;
  }

  public SearchCertificatesRestRequest criteria(List<SearchCertificateCriteriaRestRequest> criteria) {
    this.criteria = criteria;
    return this;
  }

  public SearchCertificatesRestRequest addCriteriaItem(SearchCertificateCriteriaRestRequest criteriaItem) {
    if (this.criteria == null) {
      this.criteria = new ArrayList<SearchCertificateCriteriaRestRequest>();
    }
    this.criteria.add(criteriaItem);
    return this;
  }

   /**
   * A List of search criteria.
   * @return criteria
  **/
  @Schema(description = "A List of search criteria.")
  public List<SearchCertificateCriteriaRestRequest> getCriteria() {
    return criteria;
  }

  public void setCriteria(List<SearchCertificateCriteriaRestRequest> criteria) {
    this.criteria = criteria;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchCertificatesRestRequest searchCertificatesRestRequest = (SearchCertificatesRestRequest) o;
    return Objects.equals(this.maxNumberOfResults, searchCertificatesRestRequest.maxNumberOfResults) &&
        Objects.equals(this.criteria, searchCertificatesRestRequest.criteria);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxNumberOfResults, criteria);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchCertificatesRestRequest {\n");

    sb.append("    maxNumberOfResults: ").append(toIndentedString(maxNumberOfResults)).append("\n");
    sb.append("    criteria: ").append(toIndentedString(criteria)).append("\n");
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
