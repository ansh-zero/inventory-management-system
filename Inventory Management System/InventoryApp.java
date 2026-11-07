import java.sql.*;
import java.util.Scanner;

public class InventoryApp {

    static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
    static final String USER = "root";
    static final String PASSWORD = "your_password";
    // ==================================

    static Connection conn;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("MySQL se connect ho gaya!");
            createTables();

            while (true) {
                System.out.println("\n===== Inventory Management System =====");
                System.out.println("1. Add Product");
                System.out.println("2. View All Products");
                System.out.println("3. Update Product Details");
                System.out.println("4. Delete Product");
                System.out.println("5. Stock In (stock badhao)");
                System.out.println("6. Stock Out (stock ghatao)");
                System.out.println("7. Search & Filter");
                System.out.println("8. Reports Dashboard");
                System.out.println("9. Exit");
                System.out.print("Choice: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1": addProduct(); break;
                    case "2": viewProducts(); break;
                    case "3": updateProduct(); break;
                    case "4": deleteProduct(); break;
                    case "5": changeStock(true); break;
                    case "6": changeStock(false); break;
                    case "7": searchAndFilter(); break;
                    case "8": reportsMenu(); break;
                    case "9":
                        conn.close();
                        System.out.println("Bye!");
                        return;
                    default: System.out.println("Galat choice!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ---------- SETUP ----------
    static void createTables() throws SQLException {
        String products = "CREATE TABLE IF NOT EXISTS products ("
                + "product_id INT PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "category VARCHAR(50), "
                + "price DECIMAL(10,2) NOT NULL, "
                + "quantity INT NOT NULL DEFAULT 0, "
                + "reorder_level INT NOT NULL DEFAULT 5)";

        String log = "CREATE TABLE IF NOT EXISTS stock_log ("
                + "log_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "product_id INT NOT NULL, "
                + "change_type VARCHAR(10) NOT NULL, "
                + "quantity INT NOT NULL, "
                + "log_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE)";

        try (Statement st = conn.createStatement()) {
            st.execute(products);
            st.execute(log);
        }
    }

    // ---------- INPUT HELPERS ----------
    static int readInt(String msg) {
        System.out.print(msg);
        try {
            int v = Integer.parseInt(sc.nextLine().trim());
            if (v < 0) {
                System.out.println("Negative number nahi chalega!");
                return -1;
            }
            return v;
        } catch (NumberFormatException e) {
            System.out.println("Number likho!");
            return -1;
        }
    }

    static double readDouble(String msg) {
        System.out.print(msg);
        try {
            double v = Double.parseDouble(sc.nextLine().trim());
            if (v < 0) {
                System.out.println("Negative number nahi chalega!");
                return -1;
            }
            return v;
        } catch (NumberFormatException e) {
            System.out.println("Number likho!");
            return -1;
        }
    }

    // ---------- PRODUCT MANAGEMENT ----------
    static void addProduct() {
        int id = readInt("Product ID: ");
        if (id < 0) return;
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Category: ");
        String category = sc.nextLine().trim();
        double price = readDouble("Price: ");
        if (price < 0) return;
        int qty = readInt("Opening Quantity: ");
        if (qty < 0) return;
        int reorder = readInt("Reorder Level (itne ya kam bachne par low stock): ");
        if (reorder < 0) return;

        if (name.isEmpty()) {
            System.out.println("Name khali nahi ho sakta!");
            return;
        }

        String sql = "INSERT INTO products (product_id, name, category, price, quantity, reorder_level) "
                   + "VALUES (?,?,?,?,?,?)";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, category);
                ps.setDouble(4, price);
                ps.setInt(5, qty);
                ps.setInt(6, reorder);
                ps.executeUpdate();
                if (qty > 0) logStock(id, "IN", qty);
                conn.commit();
                System.out.println("Product add ho gaya!");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            rollbackQuietly();
            System.out.println("Ye Product ID pehle se hai!");
        } catch (SQLException e) {
            rollbackQuietly();
            System.out.println("Error: " + e.getMessage());
        } finally {
            autoCommitOn();
        }
    }

    static void viewProducts() {
        queryProducts("SELECT * FROM products ORDER BY product_id");
    }

    static void updateProduct() {
        int id = readInt("Kis Product ID ko update karna hai? ");
        if (id < 0) return;
        System.out.print("Naya Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Nayi Category: ");
        String category = sc.nextLine().trim();
        double price = readDouble("Nayi Price: ");
        if (price < 0) return;
        int reorder = readInt("Naya Reorder Level: ");
        if (reorder < 0) return;

        if (name.isEmpty()) {
            System.out.println("Name khali nahi ho sakta!");
            return;
        }

        String sql = "UPDATE products SET name=?, category=?, price=?, reorder_level=? WHERE product_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setDouble(3, price);
            ps.setInt(4, reorder);
            ps.setInt(5, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Update ho gaya!" : "Product ID nahi mili.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void deleteProduct() {
        int id = readInt("Kis Product ID ko delete karna hai? ");
        if (id < 0) return;
        System.out.print("Pakka delete karna hai? Iski stock history bhi hat jayegi (y/n): ");
        String ans = sc.nextLine().trim();
        if (!ans.equalsIgnoreCase("y")) {
            System.out.println("Delete cancel kiya.");
            return;
        }
        String sql = "DELETE FROM products WHERE product_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Delete ho gaya!" : "Product ID nahi mili.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- STOCK MANAGEMENT ----------
    static void changeStock(boolean stockIn) {
        int id = readInt("Product ID: ");
        if (id < 0) return;
        int qty = readInt(stockIn ? "Kitna stock aaya? " : "Kitna stock gaya? ");
        if (qty <= 0) {
            System.out.println("Quantity 1 ya usse zyada honi chahiye!");
            return;
        }

        String sql = stockIn
                ? "UPDATE products SET quantity = quantity + ? WHERE product_id=?"
                : "UPDATE products SET quantity = quantity - ? WHERE product_id=? AND quantity >= ?";

        try {
            conn.setAutoCommit(false);   // stock update aur log ek saath honge
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, qty);
                ps.setInt(2, id);
                if (!stockIn) ps.setInt(3, qty);

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    System.out.println(stockIn
                            ? "Product ID nahi mili."
                            : "Product nahi mili ya itna stock available nahi hai.");
                    return;
                }
                logStock(id, stockIn ? "IN" : "OUT", qty);
                conn.commit();
                System.out.println("Stock update ho gaya!");
            }
        } catch (SQLException e) {
            rollbackQuietly();
            System.out.println("Error: " + e.getMessage());
            return;
        } finally {
            autoCommitOn();
        }
        showStockStatus(id);
    }

    static void logStock(int productId, String type, int qty) throws SQLException {
        String sql = "INSERT INTO stock_log (product_id, change_type, quantity) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, type);
            ps.setInt(3, qty);
            ps.executeUpdate();
        }
    }

    static void showStockStatus(int id) {
        String sql = "SELECT name, quantity, reorder_level FROM products WHERE product_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int q = rs.getInt("quantity");
                    System.out.println("Ab " + rs.getString("name") + " ka stock: " + q);
                    if (q == 0) {
                        System.out.println("WARNING: Stock khatam ho gaya!");
                    } else if (q <= rs.getInt("reorder_level")) {
                        System.out.println("WARNING: Low stock! Jaldi order karo.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- SEARCH & FILTER ----------
    static void searchAndFilter() {
        System.out.println("\n--- Search & Filter ---");
        System.out.println("1. Name se search");
        System.out.println("2. Category se filter");
        System.out.println("3. Price range se filter");
        System.out.println("4. Low stock products");
        System.out.println("5. Out of stock products");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1":
                System.out.print("Naam likho (ya uska part): ");
                String key = sc.nextLine().trim();
                queryProducts("SELECT * FROM products WHERE name LIKE ? ORDER BY product_id",
                        "%" + key + "%");
                break;
            case "2":
                System.out.print("Category likho: ");
                String cat = sc.nextLine().trim();
                queryProducts("SELECT * FROM products WHERE category LIKE ? ORDER BY product_id",
                        "%" + cat + "%");
                break;
            case "3":
                double min = readDouble("Minimum price: ");
                if (min < 0) return;
                double max = readDouble("Maximum price: ");
                if (max < 0) return;
                queryProducts("SELECT * FROM products WHERE price BETWEEN ? AND ? ORDER BY price",
                        min, max);
                break;
            case "4":
                queryProducts("SELECT * FROM products WHERE quantity > 0 AND quantity <= reorder_level "
                        + "ORDER BY quantity");
                break;
            case "5":
                queryProducts("SELECT * FROM products WHERE quantity = 0 ORDER BY product_id");
                break;
            default:
                System.out.println("Galat choice!");
        }
    }

    // ---------- REPORTS ----------
    static void reportsMenu() {
        System.out.println("\n--- Reports Dashboard ---");
        System.out.println("1. Summary Dashboard");
        System.out.println("2. Category-wise Report");
        System.out.println("3. Recent Stock History");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1": summaryReport(); break;
            case "2": categoryReport(); break;
            case "3": historyReport(); break;
            default: System.out.println("Galat choice!");
        }
    }

    static void summaryReport() {
        String sql = "SELECT COUNT(*) AS products, "
                + "COALESCE(SUM(quantity),0) AS units, "
                + "COALESCE(SUM(price * quantity),0) AS value, "
                + "COALESCE(SUM(quantity = 0),0) AS out_stock, "
                + "COALESCE(SUM(quantity > 0 AND quantity <= reorder_level),0) AS low_stock "
                + "FROM products";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("\n========== INVENTORY SUMMARY ==========");
                System.out.println("Total products     : " + rs.getLong("products"));
                System.out.println("Total units        : " + rs.getLong("units"));
                System.out.printf("Total stock value  : Rs. %.2f%n", rs.getDouble("value"));
                System.out.println("Low stock items    : " + rs.getLong("low_stock"));
                System.out.println("Out of stock items : " + rs.getLong("out_stock"));
                System.out.println("=======================================");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("\nLow ya khatam stock wale products:");
        queryProducts("SELECT * FROM products WHERE quantity <= reorder_level ORDER BY quantity");
    }

    static void categoryReport() {
        String sql = "SELECT category, COUNT(*) AS items, SUM(quantity) AS units, "
                + "SUM(price * quantity) AS value FROM products "
                + "GROUP BY category ORDER BY value DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-20s %-8s %-8s %-15s%n", "Category", "Items", "Units", "Stock Value");
            System.out.println("-----------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String cat = rs.getString("category");
                if (cat == null || cat.isEmpty()) cat = "(none)";
                System.out.printf("%-20s %-8d %-8d Rs. %-12.2f%n",
                        cat, rs.getInt("items"), rs.getInt("units"), rs.getDouble("value"));
            }
            if (!found) System.out.println("Koi data nahi hai.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void historyReport() {
        String sql = "SELECT l.log_id, p.name, l.change_type, l.quantity, l.log_date "
                + "FROM stock_log l JOIN products p ON l.product_id = p.product_id "
                + "ORDER BY l.log_id DESC LIMIT 10";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-8s %-25s %-8s %-8s %-20s%n", "Log ID", "Product", "Type", "Qty", "Date");
            System.out.println("-------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8d %-25s %-8s %-8d %-20s%n",
                        rs.getInt("log_id"), rs.getString("name"),
                        rs.getString("change_type"), rs.getInt("quantity"),
                        rs.getTimestamp("log_date"));
            }
            if (!found) System.out.println("Abhi koi stock history nahi hai.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- COMMON HELPERS ----------
    static void queryProducts(String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                printProducts(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void printProducts(ResultSet rs) throws SQLException {
        System.out.printf("%-6s %-25s %-15s %-10s %-6s %-8s %-12s%n",
                "ID", "Name", "Category", "Price", "Qty", "Reorder", "Status");
        System.out.println("--------------------------------------------------------------------------------------");
        boolean found = false;
        while (rs.next()) {
            found = true;
            int qty = rs.getInt("quantity");
            int reorder = rs.getInt("reorder_level");
            String status = qty == 0 ? "OUT OF STOCK" : (qty <= reorder ? "LOW" : "OK");
            System.out.printf("%-6d %-25s %-15s %-10.2f %-6d %-8d %-12s%n",
                    rs.getInt("product_id"), rs.getString("name"),
                    rs.getString("category"), rs.getDouble("price"),
                    qty, reorder, status);
        }
        if (!found) System.out.println("Koi product nahi mila.");
    }

    static void rollbackQuietly() {
        try { conn.rollback(); } catch (SQLException ignored) {}
    }

    static void autoCommitOn() {
        try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
    }
}