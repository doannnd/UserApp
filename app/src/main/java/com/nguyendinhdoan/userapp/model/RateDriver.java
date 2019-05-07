package com.nguyendinhdoan.userapp.model;

public class RateDriver {

    private String rates;
    private String comments;

    public RateDriver() {
    }

    public RateDriver(String rates, String comments) {
        this.rates = rates;
        this.comments = comments;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
