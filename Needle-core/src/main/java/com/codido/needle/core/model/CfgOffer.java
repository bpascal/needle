package com.codido.needle.core.model;

public class CfgOffer {
    private Long offerId;

    public CfgOffer(Long offerId) {
        this.offerId = offerId;
    }

    public CfgOffer() {
        super();
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }
}