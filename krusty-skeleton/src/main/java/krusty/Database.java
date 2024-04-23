package krusty;

import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.sqlite.SQLiteException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class Database {
	
	private static final String jdbcString = "jdbc:mysql://puccini.cs.lth.se/hbg29";
	private static final String jdbcUsername = "hbg29";
	private static final String jdbcPassword = "pqx717bq";

	private Connection conn;

    public Database() {
        conn = null;
    }

	public void connect() {
		try {       	
            conn = DriverManager.getConnection(jdbcString, jdbcUsername, jdbcPassword);
        }
        catch (SQLException e) {
            System.err.println(e);
            e.printStackTrace();
        }
	}

	// TODO: Implement and change output in all methods below!

	public String getCustomers(Request req, Response res) throws SQLException{
		String sql = "select * from WholesaleCustomer";

		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ResultSet rs = ps.executeQuery();
			String json = Jsonizer.toJson(rs, "customers");
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{}";
	}

	public String getRawMaterials(Request req, Response res) {
		return "{}";
	}

// joachim 2024-04-19 behövde getCookies för att kunna börja jobba på get createPallet
public String getCookies(Request req, Response res) {
    String sql = "SELECT * FROM Cookie";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ResultSet rs = ps.executeQuery();
        String json = Jsonizer.toJson(rs, "cookies");
        return json;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "{\"cookies\":[]}";
}

	public String getRecipes(Request req, Response res) throws SQLException{
		String sql = "select cookieName, ingredientName, amount, unit from Recipe" + 
					"JOIN Ingredient on Recipe.ingredientName = Ingredient.name;";

		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ResultSet rs = ps.executeQuery();
			String json = Jsonizer.toJson(rs, "recipes");
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{}";
	}

	//Inte klar än
	public String getPallets(Request req, Response res) {
		String cookie = req.queryParams("cookie");
		String from = req.queryParams("from");
		String to = req.queryParams("to");
		String blocked = req.queryParams("blocked");
		
		String sqlQuery = "SELECT p.palletID AS id, p.cookieName AS cookie, p.productionDate AS production_date, "+
							"c.name AS customer, p.isBlocked AS blocked " +
						"FROM Pallets p " +
						"INNER JOIN Orders o ON p.orderID = o.orderID " +
						"INNER JOIN WholesaleCustomer c ON o.customerID = c.customerID;";

		try (PreparedStatement ps = conn.prepareStatement(sqlQuery)){
			ResultSet rs = ps.executeQuery();
			String json = Jsonizer.toJson(rs, "pallets");
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{\"pallets\":[]}";
	}

	public String reset(Request req, Response res) {

		// Test to se if File and BufferedReader works
		// String sqlFile = "initial-data.sql";

		// try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
		// 	StringBuilder queryBuilder = new StringBuilder();
		// 	String line;
		// 	while ((line = reader.readLine()) != null){
		// 		System.out.println(line);
		// 	}
		// }
		// catch(IOException e){
		// 	System.out.println(e);
		// }
		// return "{}";
		
		String sqlFile = "initial-data.sql";

		try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
			StringBuilder queryBuilder = new StringBuilder();
			String line;
	
			while ((line = reader.readLine()) != null) {
				// Ignorera tomma rader och kommentarer
				if (!line.trim().isEmpty() && !line.startsWith("--")) {
					queryBuilder.append(line);
	
					// Om det finns en semikolon, exekvera frågan
					if (line.endsWith(";")) {
						String query = queryBuilder.toString();
						try (PreparedStatement ps = conn.prepareStatement(query)) {
							ps.executeUpdate();
						} catch (SQLException e) {
							e.printStackTrace();
							return Jsonizer.anythingToJson(e.getMessage(), "status");
						}
						// Återställ StringBuilder för nästa fråga
						queryBuilder = new StringBuilder();
					}
				}
			}
			return Jsonizer.anythingToJson("ok", "status");
		} catch (IOException e) {
			e.printStackTrace();
			return Jsonizer.anythingToJson(e.getMessage(), "status");
		}
	}

	public String createPallet(Request req, Response res) throws SQLException {
		String selectedCookie = req.queryParams("cookie");
	
		String createPallet = "INSERT INTO Pallets (isBlocked, productionDate, cookieName) VALUES (0, NOW(), ?)";
		String updateIngredients = "UPDATE Ingredient " +
								"SET quantityTotal = quantityTotal - IFNULL( " +
								"    (SELECT 54 * amount " +
								"     FROM Recipe " +
								"     WHERE cookieName = ? " +
								"       AND Ingredient.name = Recipe.ingredientName " +
								"    ), 0 " +
								");";

		conn.setAutoCommit(false);
	
		try (PreparedStatement ps = conn.prepareStatement(createPallet);
			 PreparedStatement psUpdate = conn.prepareStatement(updateIngredients)) {
			// Insert new pallet
			ps.setString(1, selectedCookie);
			int palletInserted = ps.executeUpdate();
	
			// Update ingredients
			psUpdate.setString(1, selectedCookie);
			int ingredientsUpdated = psUpdate.executeUpdate();
	
			if (palletInserted > 0 && ingredientsUpdated > 0) {
				conn.commit();
				return "{\"status\": \"ok\",\"id\": " + getPalletId() + "}";
			} else {
				conn.rollback();
			}
		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();
		}
	
		return "{}";
	}
	
	// Helper method to retrieve the ID of the last inserted pallet
	private int getPalletId() throws SQLException {
		String query = "SELECT LAST_INSERT_ID() AS last_id";
		try (PreparedStatement ps = conn.prepareStatement(query);
			 ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getInt("last_id");
			}
		}
		throw new SQLException("Unable to retrieve pallet ID");
	}
	
}
