package warungaju;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceData {
    public List<TransactionData> transactions = new ArrayList<>();
    public List<ProductData> products = new ArrayList<>();
    public SimulationSettings simulation = new SimulationSettings();
    public CashFlowSettings cashFlow = new CashFlowSettings();
    public boolean actualDataImported;

    public void normalize() {
        if (transactions == null) transactions = new ArrayList<>();
        if (products == null) products = new ArrayList<>();
        if (simulation == null) simulation = new SimulationSettings();
        if (cashFlow == null) cashFlow = new CashFlowSettings();
    }

    public List<Transaksi> toTransactions() {
        List<Transaksi> result = new ArrayList<>();
        for (TransactionData item : transactions) {
            if (item != null && item.date != null) {
                try {
                    result.add(new Transaksi(LocalDate.parse(item.date), item.type,
                        item.category, item.amount, item.hpp, item.description, item.product));
                } catch (ValidasiException | java.time.format.DateTimeParseException ex) {
                    // Baris rusak dari cloud dilewati supaya satu data lama tidak
                    // menggagalkan seluruh workspace. Data tetap utuh di Supabase.
                    System.err.println("Transaksi dilewati (" + item.date + "): " + ex.getMessage());
                }
            }
        }
        return result;
    }

    public List<Produk> toProducts() {
        List<Produk> result = new ArrayList<>();
        for (ProductData item : products) {
            if (item != null) {
                result.add(new Produk(item.name, item.productionCost, item.sellingPrice,
                    item.stock, item.productionQuantity));
            }
        }
        return result;
    }

    public static class TransactionData {
        public String date;
        public String type;
        public String category;
        public double amount;
        public double hpp;
        public String description;
        public String product;

        public TransactionData() {
        }

        public TransactionData(Transaksi transaksi) {
            date = transaksi.getTanggal().toString();
            type = transaksi.getJenis();
            category = transaksi.getKategori();
            amount = transaksi.getNominal();
            hpp = transaksi.getHpp();
            description = transaksi.getKeterangan();
            product = transaksi.getProduk();
        }
    }

    public static class ProductData {
        public String name;
        public double productionCost;
        public double sellingPrice;
        public int stock;
        public int productionQuantity;

        public ProductData() {
        }

        public ProductData(Produk produk) {
            name = produk.getNama();
            productionCost = produk.getBiayaProduksi();
            sellingPrice = produk.getHargaJual();
            stock = produk.getStok();
            productionQuantity = produk.getJumlahProduksi();
        }
    }

    public static class SimulationSettings {
        public double ingredientPrice = 9000;
        public int employeeCount = 2;
        public double salaryPerEmployee = 2500000;
        public double operatingHours = 10;
        public double monthlySalesTarget = 100;
        public double sellingPrice = 18000;
    }

    public static class CashFlowSettings {
        public double startingBalance = 3500000;
        public double monthlyIncome = 1250000;
        public double monthlyExpenses = 650000;
    }
}
