package nl.rabobank.account;

public class PaymentAccount extends Account {
    public PaymentAccount(String accountNumber, String ownerId, AccountType type) {
        super(accountNumber, ownerId, type);
    }
}