import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.*;
import java.io.Serializable;
import javax.swing.*;
import java.awt.*;

public class project extends JFrame {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/electronics_store";
    private static final String USER = "newuser";
    private static final String PASS = "adithyan";

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load MariaDB driver: " + e.getMessage());
            System.exit(1);
        }
    }

    // Helper method to get database connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public project() {
        setTitle("Electronics Store");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add buttons and other UI components to the panel
        JButton insertButton = new JButton("Insert");
        insertButton.addActionListener(e -> insert());
        panel.add(insertButton);

        JButton deleteButton = new JButton("Delete Product");
        deleteButton.addActionListener(e -> delete());
        panel.add(deleteButton);

        JButton updateButton = new JButton("Update Product");
        updateButton.addActionListener(e -> updateProductName());
        panel.add(updateButton);

        JButton searchButton = new JButton("Search by Name");
        searchButton.addActionListener(e -> searchProductByName());
        panel.add(searchButton);

        JButton viewAllButton = new JButton("View All Products");
        viewAllButton.addActionListener(e -> viewAllProducts());
        panel.add(viewAllButton);

        JButton calculateBillButton = new JButton("Calculate Total Bill");
        calculateBillButton.addActionListener(e -> calculateTotalBill());
        panel.add(calculateBillButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        panel.add(exitButton);

        add(panel, BorderLayout.NORTH);
    }

    public void insert() {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO products (serial_no, product_name, price, stock) VALUES (?, ?, ?, ?)";

            JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField serialNoField = new JTextField();
            JTextField productNameField = new JTextField();
            JTextField priceField = new JTextField();
            JTextField stockField = new JTextField();

            inputPanel.add(new JLabel("Serial No:"));
            inputPanel.add(serialNoField);
            inputPanel.add(new JLabel("Product Name:"));
            inputPanel.add(productNameField);
            inputPanel.add(new JLabel("Price:"));
            inputPanel.add(priceField);
            inputPanel.add(new JLabel("Stock:"));
            inputPanel.add(stockField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add Product", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                int serialNo = Integer.parseInt(serialNoField.getText());
                String productName = productNameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, serialNo);
                pstmt.setString(2, productName);
                pstmt.setDouble(3, price);
                pstmt.setInt(4, stock);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product added successfully");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void delete() {
    try (Connection conn = getConnection()) {
        String sql = "DELETE FROM products WHERE serial_no = ?";

        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField serialNoField = new JTextField();
        inputPanel.add(new JLabel("Serial No:"));
        inputPanel.add(serialNoField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Delete Product", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String serialNoText = serialNoField.getText().trim();
            if (serialNoText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a serial number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int serialNo = Integer.parseInt(serialNoText);

                // Check if product exists
                String checkSql = "SELECT * FROM products WHERE serial_no = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, serialNo);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            JOptionPane.showMessageDialog(this, "No product found with this serial number.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Show product details and confirm deletion
                        String productDetails = String.format(
                            "Serial No: %d\nProduct Name: %s\nPrice: %.2f\nStock: %d",
                            rs.getInt("serial_no"),
                            rs.getString("product_name"),
                            rs.getDouble("price"),
                            rs.getInt("stock")
                        );

                        int confirmResult = JOptionPane.showConfirmDialog(this,
                                "Are you sure you want to delete this product?\n\n" + productDetails,
                                "Confirm Deletion",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if (confirmResult != JOptionPane.YES_OPTION) {
                            return; // User canceled the deletion
                        }
                    }
                }

                // Proceed with deletion
                try (PreparedStatement deleteStmt = conn.prepareStatement(sql)) {
                    deleteStmt.setInt(1, serialNo);
                    int rowsAffected = deleteStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Product deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete product. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid serial number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    public void updateProductName() {
    try (Connection conn = getConnection()) {
        String sql = "UPDATE products SET product_name = ? WHERE serial_no = ?";

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField serialNoField = new JTextField();
        JTextField productNameField = new JTextField();

        inputPanel.add(new JLabel("Serial No:"));
        inputPanel.add(serialNoField);
        inputPanel.add(new JLabel("New Product Name:"));
        inputPanel.add(productNameField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Update Product Name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String serialNoText = serialNoField.getText().trim();
            String newProductName = productNameField.getText().trim();

            if (serialNoText.isEmpty() || newProductName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both serial number and new product name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int serialNo = Integer.parseInt(serialNoText);

                // Check if product exists
                String checkSql = "SELECT * FROM products WHERE serial_no = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, serialNo);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            JOptionPane.showMessageDialog(this, "No product found with this serial number.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Show current product details and confirm update
                        String currentDetails = String.format(
                            "Serial No: %d\nCurrent Product Name: %s",
                            rs.getInt("serial_no"),
                            rs.getString("product_name")
                        );

                        int confirmResult = JOptionPane.showConfirmDialog(this,
                                "Are you sure you want to update this product's name?\n\n" + currentDetails,
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if (confirmResult != JOptionPane.YES_OPTION) {
                            return; // User canceled the update
                        }
                    }
                }

                // Proceed with the update
                try (PreparedStatement updateStmt = conn.prepareStatement(sql)) {
                    updateStmt.setString(1, newProductName);
                    updateStmt.setInt(2, serialNo);
                    int rowsAffected = updateStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Product name updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update product name. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid serial number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
   
    void searchProductByName() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM products WHERE product_name LIKE ?";

            JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField productNameField = new JTextField();

            inputPanel.add(new JLabel("Product Name:"));
            inputPanel.add(productNameField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Search Product", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String productName = productNameField.getText();

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, "%" + productName + "%");

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                        "Serial No: " + rs.getInt("serial_no") + "\n" +
                        "Product Name: " + rs.getString("product_name") + "\n" +
                        "Price: " + rs.getDouble("price") + "\n" +
                        "Stock: " + rs.getInt("stock"),
                        "Product Details", JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void viewAllProducts() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("Serial No: ").append(rs.getInt("serial_no")).append("\n");
                sb.append("Product Name: ").append(rs.getString("product_name")).append("\n");
                sb.append("Price: ").append(rs.getDouble("price")).append("\n");
                sb.append("Stock: ").append(rs.getInt("stock")).append("\n");
                sb.append("------------------------\n");
            }

            if (sb.length() == 0) {
                JOptionPane.showMessageDialog(this, "No products found in database", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JTextArea textArea = new JTextArea(sb.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(this, scrollPane, "All Products", JOptionPane.PLAIN_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void calculateTotalBill() {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM products WHERE product_name LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField productNameField = new JTextField();

            inputPanel.add(new JLabel("Product Names (separated by commas):"));
            inputPanel.add(productNameField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Calculate Total Bill", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String[] products = productNameField.getText().split(",");
                double totalBill = 0.0;

                for (String product : products) {
                    pstmt.setString(1, "%" + product.trim() + "%");
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        System.out.println("Serial No: " + rs.getInt("serial_no"));
                        System.out.println("Product Name: " + rs.getString("product_name"));
                        System.out.println("Price: " + rs.getDouble("price"));
                        System.out.println("Stock: " + rs.getInt("stock"));
                        System.out.println("------------------------");
                        totalBill += rs.getDouble("price");
                    }
                }

                if (totalBill == 0.0) {
                    JOptionPane.showMessageDialog(this, "No products found", "Information", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Total Bill: $" + totalBill, "Bill Summary", JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            project frame = new project();
            frame.setVisible(true);
        });
    }
}