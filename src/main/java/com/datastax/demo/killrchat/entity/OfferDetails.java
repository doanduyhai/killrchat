package com.datastax.demo.killrchat.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Enumerated;
import info.archinnov.achilles.annotations.UDT;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@UDT(name = "offer_details")
public class OfferDetails {

    @Column
    @NotBlank(message = "advertiser id cannot be blank")
    private String advertiserId;

    @Column
    @NotBlank(message = "offer name cannot be blank")
    private String offerName;

    @Column
    private String description;

    /**
     * Link to landing page with no geo targeting so Affiliates can see landing page example.
     */
    @NotBlank(message = "previewUrl cannot be blank")
    @Column
    private String previewUrl;


    @NotBlank(message = "offerUrl cannot be blank")
    @Column
    private String offerUrl;

    @NotNull(message = "conversionTracking cannot be null")
    @Enumerated
    private String conversionTracking;

    @NotNull(message = "status cannot be null")
    @Enumerated
    private String status;

    /**
     * Offer will expire at 11:59 pm of selected date.
     */
    @Column
    private Date expiryDate;

    /**
     * Assign a reference ID to this offer and pass this value into Offer URLs.
     */
    @Column
    private String referenceId;

    /**
     * Categorize offer for Affiliates to search and group by.
     */
    @Column
    private List<String> categoryIds;

    /**
     * The contents of this note will not be displayed to Affiliates
     */
    @Column
    private String notes;

    public String getAdvertiserId() {
        return advertiserId;
    }

    public void setAdvertiserId(String advertiserId) {
        this.advertiserId = advertiserId;
    }

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getOfferUrl() {
        return offerUrl;
    }

    public void setOfferUrl(String offerUrl) {
        this.offerUrl = offerUrl;
    }

    public String getConversionTracking() {
        return conversionTracking;
    }

    public void setConversionTracking(String conversionTracking) {
        this.conversionTracking = conversionTracking;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
