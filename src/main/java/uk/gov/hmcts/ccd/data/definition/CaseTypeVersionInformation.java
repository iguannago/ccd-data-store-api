package uk.gov.hmcts.ccd.data.definition;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CaseTypeVersionInformation {

    private Integer version;

    public CaseTypeVersionInformation() {}

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
