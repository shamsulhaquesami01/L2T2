package CSE214.STRUCTURAL.templates;

// ==========================================
// BRIDGE + ADAPTER
// Building a Bridge, but ONE Implementor variant is an existing/legacy
// class with mismatched method names -- adapt it to fit the Implementor
// interface, and it slots in exactly like a native concrete implementor.
// (Extends the payment theme from your own AdapterDemo.java.)
// ==========================================

// ---- Bridge: Implementor interface (what the Abstraction expects) ----
interface BankProcessor {
    boolean charge(double amount);
}

// A "native" implementor, written to the interface directly
class ModernBank implements BankProcessor {
    public boolean charge(double amount) {
        System.out.println("ModernBank: charged $" + amount + " directly.");
        return true;
    }
}

// ---- Legacy class with an INCOMPATIBLE interface -- cannot modify ----
class OldRegionalBank {
    // returns a transaction code: "00" = approved, anything else = declined
    String submitTransaction(int amountInCents) {
        System.out.println("OldRegionalBank: submitted " + amountInCents + " cents.");
        return "00";
    }
}

// ---- Adapter: makes OldRegionalBank usable wherever BankProcessor is expected ----
class OldBankAdapter implements BankProcessor {
    private OldRegionalBank legacy;

    public OldBankAdapter(OldRegionalBank legacy) {
        this.legacy = legacy;
    }

    public boolean charge(double amount) {
        int cents = (int) Math.round(amount * 100);
        String code = legacy.submitTransaction(cents);
        return code.equals("00");
    }
}

// ---- Bridge: Abstraction -- doesn't care whether BankProcessor is native or adapted ----
abstract class PaymentChannel {
    protected BankProcessor processor;
    public PaymentChannel(BankProcessor processor) { this.processor = processor; }
    public abstract boolean pay(double amount);
}

class CardChannel extends PaymentChannel {
    public CardChannel(BankProcessor processor) { super(processor); }
    public boolean pay(double amount) {
        System.out.println("Processing card payment...");
        return processor.charge(amount);
    }
}

class WalletChannel extends PaymentChannel {
    public WalletChannel(BankProcessor processor) { super(processor); }
    public boolean pay(double amount) {
        System.out.println("Processing wallet payment...");
        return processor.charge(amount);
    }
}

// ---- Demo ----
public class BridgeAdapterDemo {
    public static void main(String[] args) {
        PaymentChannel card = new CardChannel(new ModernBank());
        System.out.println("Success: " + card.pay(45.50));

        System.out.println();

        // legacy bank plugged into the SAME Bridge, via an Adapter --
        // CardChannel/WalletChannel never know the difference
        PaymentChannel wallet = new WalletChannel(new OldBankAdapter(new OldRegionalBank()));
        System.out.println("Success: " + wallet.pay(45.50));
    }
}
