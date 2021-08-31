package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PaymentToken {
    @SerializedName("token")
    @Expose
    private String paymentToken;
    @SerializedName("amount")
    @Expose
    private long amount;
    @SerializedName("phone")
    @Expose
    private long phone;

    public PaymentToken(String paymentToken, long amount, long phone) {
        this.paymentToken = paymentToken;
        this.amount = amount;
        this.phone = phone;
    }
}
