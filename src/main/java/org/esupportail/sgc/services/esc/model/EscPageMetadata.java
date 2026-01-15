package org.esupportail.sgc.services.esc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("PageMetadata")
public class EscPageMetadata {

  private Long number;

  private Long size;

  private Long totalElements;

  private Long totalPages;

  public EscPageMetadata number(Long number) {
    this.number = number;
    return this;
  }

  @JsonProperty("number")
  public Long getNumber() {
    return number;
  }

  public void setNumber(Long number) {
    this.number = number;
  }

  public EscPageMetadata size(Long size) {
    this.size = size;
    return this;
  }

  @JsonProperty("size")
  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public EscPageMetadata totalElements(Long totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  @JsonProperty("totalElements")
  public Long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(Long totalElements) {
    this.totalElements = totalElements;
  }

  public EscPageMetadata totalPages(Long totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  @JsonProperty("totalPages")
  public Long getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Long totalPages) {
    this.totalPages = totalPages;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EscPageMetadata pageMetadata = (EscPageMetadata) o;
    return Objects.equals(this.number, pageMetadata.number) &&
        Objects.equals(this.size, pageMetadata.size) &&
        Objects.equals(this.totalElements, pageMetadata.totalElements) &&
        Objects.equals(this.totalPages, pageMetadata.totalPages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(number, size, totalElements, totalPages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EscPageMetadata {\n");
    sb.append("    number: ").append(toIndentedString(number)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

