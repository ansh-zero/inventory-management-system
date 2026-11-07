# 📦 Inventory Management System

> A console-based Java application to manage products, stock levels, and inventory reports using a MySQL database.
> Built as **Task 3 (Medium)** of the **Auspify Technologies Software Development Internship**.

---

## 📌 Project Name

**Inventory Management System (Java + MySQL)**

---

## 📝 Project Description

The Inventory Management System helps a shop or warehouse keep track of its products and stock levels. It replaces manual stock registers with a database-driven application.

Users can add products, update stock when new items arrive or are sold, search and filter products in different ways, and view reports such as total stock value, low stock items, and category-wise summaries. Every stock change is recorded in a stock history table, so the movement of goods can always be traced.

Stock updates use **database transactions (commit / rollback)**, so the product quantity and the stock log are always updated together. The system also prevents stock from going below zero and shows **low stock warnings** based on a reorder level set for each product.

---

## 🛠️ Technologies Used

| Component | Technology |
|---|---|
| Programming Language | Java (JDK 8 or above) |
| Database | MySQL |
| Database Connectivity | JDBC (MySQL Connector/J 8.0.33) |
| Database Tool | MySQL Workbench |
| Editor / IDE | Notepad / VS Code |
| Version Control | Git & GitHub |

---

## ✨ Features

- ➕ **Add Product:** Store product ID, name, category, price, opening quantity, and reorder level
- 📋 **View All Products:** Table view with a live stock status (`OK`, `LOW`, `OUT OF STOCK`)
- ✏️ **Update Product Details:** Change name, category, price, and reorder level
- 🗑️ **Delete Product:** Remove a product (with confirmation)
- 📥 **Stock In:** Increase stock when new items arrive
- 📤 **Stock Out:** Reduce stock when items are sold (blocked if stock is insufficient)
- ⚠️ **Low Stock Warnings:** Alerts when stock reaches the reorder level or hits zero
- 🔍 **Search & Filter:**
  - Search by product name
  - Filter by category
  - Filter by price range
  - View low stock products
  - View out of stock products
- 📊 **Reports Dashboard:**
  - Summary (total products, total units, total stock value, low stock count, out of stock count)
  - Category-wise report
  - Recent stock history
- ✅ **Validation:** Handles duplicate IDs, empty names, invalid or negative numbers, and insufficient stock
- 🔄 **Transactions:** Commit and rollback keep stock data accurate
- 🔒 **Secure Queries:** Uses `PreparedStatement` to prevent SQL injection

---

## 🗄️ Database Structure

**Database:** `inventory_db`

### Table: `products`

| Column | Data Type | Description |
|---|---|---|
| product_id | INT | Primary Key |
| name | VARCHAR(100) | Product name (required) |
| category | VARCHAR(50) | Product category |
| price | DECIMAL(10,2) | Price per unit |
| quantity | INT | Current stock |
| reorder_level | INT | Stock at or below this is marked as low |

### Table: `stock_log`

| Column | Data Type | Description |
|---|---|---|
| log_id | INT | Primary Key (auto increment) |
| product_id | INT | Foreign Key → `products.product_id` (cascade delete) |
| change_type | VARCHAR(10) | `IN` or `OUT` |
| quantity | INT | Units added or removed |
| log_date | DATETIME | Date and time of the change (automatic) |

> Both tables are created automatically when the program runs for the first time.

---

## 🔄 How Stock Updates Work

**Stock In**
1. Increase the product quantity
2. Add an `IN` entry in `stock_log`
3. Commit (if anything fails, everything is rolled back)

**Stock Out**
1. Reduce the quantity only if enough stock is available
2. Add an `OUT` entry in `stock_log`
3. Commit (if stock is insufficient, nothing changes)

---

## 📁 Project Structure

```
inventory-management-system/
 ├── InventoryApp.java
 ├── README.md
 └── screenshots/
      ├── home.png
      ├── add_product.png
      ├── view_products.png
      ├── stock_update.png
      ├── search_filter.png
      └── dashboard.png
```

---

## 📸 Screenshots

### Home Menu
![Home Menu](screenshots/home.png)

### Add Product
![Add Product](screenshots/add_product.png)

### View All Products
![View Products](screenshots/view_products.png)

### Stock Update (Stock In / Stock Out)
![Stock Update](screenshots/stock_update.png)

### Search & Filter
![Search and Filter](screenshots/search_filter.png)

### Reports Dashboard
![Dashboard](screenshots/dashboard.png)

---

## ⚙️ Installation Steps

### 1. Install the prerequisites

- **JDK** (Java Development Kit). Check with:
  ```
  java -version
  ```
- **MySQL Server** and **MySQL Workbench**
- **MySQL Connector/J** jar file (`mysql-connector-j-8.0.33.jar`)

### 2. Clone the repository

```
git clone https://github.com/ansh-zero/inventory-management-system.git
cd inventory-management-system
```

*(Or simply download the project as a ZIP and extract it.)*

### 3. Add the MySQL Connector

[Click here to download mysql-connector-j-8.0.33.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar)

Place the jar in the same folder as `InventoryApp.java` (or keep it anywhere and use its full path when running).

### 4. Create the database

Open MySQL Workbench and run:

```sql
CREATE DATABASE inventory_db;
```

### 5. Configure your MySQL credentials

Open `InventoryApp.java` and update these lines with your own details:

```java
static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
static final String USER = "root";
static final String PASSWORD = "your_password";
```

---

## ▶️ How to Run the Project

Open Command Prompt / Terminal inside the project folder.

**Step 1: Compile**
```
javac InventoryApp.java
```

**Step 2: Run**

Windows:
```
java -cp ".;mysql-connector-j-8.0.33.jar" InventoryApp
```

Mac / Linux:
```
java -cp ".:mysql-connector-j-8.0.33.jar" InventoryApp
```

On successful connection you will see:

```
MySQL se connect ho gaya!

===== Inventory Management System =====
1. Add Product
2. View All Products
3. Update Product Details
4. Delete Product
5. Stock In (stock badhao)
6. Stock Out (stock ghatao)
7. Search & Filter
8. Reports Dashboard
9. Exit
```

> **Note:** MySQL Server must be running. If you get `Communications link failure`, start the MySQL service (`net start MySQL80` on Windows).

---

## 🧪 Testing Performed

- Added multiple products across different categories
- Viewed products and checked the stock status column
- Increased and reduced stock and verified the updated quantity
- Tried to remove more stock than available (blocked correctly)
- Verified low stock and out of stock warnings
- Used search by name, filter by category, filter by price range, low stock and out of stock views
- Generated the summary, category-wise, and stock history reports
- Updated and deleted products
- Checked data accuracy in MySQL Workbench using `SELECT * FROM products;` and `SELECT * FROM stock_log;`

---

## 🚀 Future Improvements

- Java Swing GUI
- User login with admin and staff roles
- Supplier management and purchase orders
- Export reports to CSV / PDF
- Automatic email alert for low stock

---

## 📚 What I Learned

- Designing a database with related tables
- Writing business logic for stock management
- Handling transactions with `commit` and `rollback`
- Using `GROUP BY`, `SUM`, `JOIN`, and `BETWEEN` for reports and filtering
- Writing secure queries with `PreparedStatement`
- Validating input and testing data accuracy

---

## 📬 Contact

**GitHub:** [https://github.com/ansh-zero](https://github.com/ansh-zero)

Feel free to open an issue or reach out for suggestions and feedback.

---

## 🏢 About the Internship

This project was completed as part of the **Software Development Internship** at **Auspify Technologies**.

`#Auspify` `#AuspifyTechnologies` `#AuspifyInternship` `#AuspifyProjects`
