package org.esupportail.sgc.services.esc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.esupportail.sgc.domain.EscPerson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonTypeName("PagedResourcesPersonLiteView")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EscPagedResourcesPersonLiteView {

  private List<EscPerson> content = new ArrayList<>();

  private EscPageMetadata page;

  @JsonProperty("content")
  public List<EscPerson> getContent() {
    return content;
  }

  public void setContent(List<EscPerson> content) {
    this.content = content;
  }


  public EscPagedResourcesPersonLiteView page(EscPageMetadata page) {
    this.page = page;
    return this;
  }

  @JsonProperty("page")
  public EscPageMetadata getPage() {
    return page;
  }

  public void setPage(EscPageMetadata page) {
    this.page = page;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EscPagedResourcesPersonLiteView pagedResourcesPersonLiteView = (EscPagedResourcesPersonLiteView) o;
    return Objects.equals(this.content, pagedResourcesPersonLiteView.content) &&
        Objects.equals(this.page, pagedResourcesPersonLiteView.page);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, page);
  }


}

