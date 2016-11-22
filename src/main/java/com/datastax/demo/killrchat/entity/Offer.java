package com.datastax.demo.killrchat.entity;

import com.datastax.driver.core.utils.UUIDs;
import info.archinnov.achilles.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table
public class Offer {

    @ClusteringColumn
    protected UUID id = UUIDs.timeBased();
    // protected UUID id = UUIDs.random();

    @Column
    protected Long timestamp = new Date().getTime();

    @Column
    protected Date createdDate = new Date();

    @Column
    protected Date lastModifiedDate = new Date();

    @Column
    protected Boolean active = Boolean.TRUE;
    @Column
    private Long offerCounter;

    @PartitionKey
    @Column
    private String tenantId;

    @Column
    @Frozen
    private OfferDetails details;

    @Column
//    @Frozen
    private String currencyDetails;

    @Column
//    @Frozen
    private String revenueDetails;

    @Column
//    @Frozen
    private String payoutDetails;

    @Column
    private List<String> goalId;

    @Column
    private Boolean multipleGoalEnable;

    @Column
//    @Frozen
    private String settings;

    @Column
//    @Frozen
    private String trackingDetails;

    @Column
//    @Frozen
    private String offerOptionalTracking;

    @Column
//    @Frozen
    private String offerTargeting;

    @Column
    @Index
    @Frozen
    private List<String> blockedAffiliateIds;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getOfferCounter() {
        return offerCounter;
    }

    public void setOfferCounter(Long offerCounter) {
        this.offerCounter = offerCounter;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public OfferDetails getDetails() {
        return details;
    }

    public void setDetails(OfferDetails details) {
        this.details = details;
    }

    public String getCurrencyDetails() {
        return currencyDetails;
    }

    public void setCurrencyDetails(String currencyDetails) {
        this.currencyDetails = currencyDetails;
    }

    public String getRevenueDetails() {
        return revenueDetails;
    }

    public void setRevenueDetails(String revenueDetails) {
        this.revenueDetails = revenueDetails;
    }

    public String getPayoutDetails() {
        return payoutDetails;
    }

    public void setPayoutDetails(String payoutDetails) {
        this.payoutDetails = payoutDetails;
    }

    public List<String> getGoalId() {
        return goalId;
    }

    public void setGoalId(List<String> goalId) {
        this.goalId = goalId;
    }

    public Boolean getMultipleGoalEnable() {
        return multipleGoalEnable;
    }

    public void setMultipleGoalEnable(Boolean multipleGoalEnable) {
        this.multipleGoalEnable = multipleGoalEnable;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public String getTrackingDetails() {
        return trackingDetails;
    }

    public void setTrackingDetails(String trackingDetails) {
        this.trackingDetails = trackingDetails;
    }

    public String getOfferOptionalTracking() {
        return offerOptionalTracking;
    }

    public void setOfferOptionalTracking(String offerOptionalTracking) {
        this.offerOptionalTracking = offerOptionalTracking;
    }

    public String getOfferTargeting() {
        return offerTargeting;
    }

    public void setOfferTargeting(String offerTargeting) {
        this.offerTargeting = offerTargeting;
    }

    public List<String> getBlockedAffiliateIds() {
        return blockedAffiliateIds;
    }

    public void setBlockedAffiliateIds(List<String> blockedAffiliateIds) {
        this.blockedAffiliateIds = blockedAffiliateIds;
    }
}
