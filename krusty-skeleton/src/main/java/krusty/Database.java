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

	public String getCookies(Request req, Response res) {
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
						// Återställ strängbyggaren för nästa fråga
						queryBuilder = new StringBuilder();
					}
				}
			}
			return Jsonizer.anythingToJson("ok", "status");
		} catch (IOException e) {
			e.printStackTrace();
			return Jsonizer.anythingToJson(e.getMessage(), "status");
		}




		// while((row = r.readLine()) != null){
			
		// }

		// String q1 = "set foreign_key_checks = 0";
		// String q2 = "truncate table WholesaleCustomer";
		// String q3 = "truncate table Orders";
		// String q4 = "truncate table Pallets";
		// String q5 = "truncate table Ingredient";
		// String q6 = "truncate table Cookie";
		// String q7 = "truncate table CookieOrder";
		// String q8 = "truncate table Recipe";
		// String q9 = "set foreign_key_checks = 1";
		// String q10 = "INSERT INTO WholesaleCustomer(name, address) VALUES" +
		// 		"('Bjudkakor AB', 'Ystad')," +
		// 		"('Finkakor AB', 'Helsingborg')," +
		// 		"('Gästkakor AB', 'Hässleholm')," +
		// 		"('Kaffebröd AB', 'Landskrona')," +
		// 		"('Kalaskakor AB', 'Trelleborg')," +
		// 		"('Partykakor AB', 'Kristianstad')," +
		// 		"('Skånekakor AB', 'Perstorp')," +
		// 		"('Småbröd AB', 'Malmö')";
		// String q11 = "INSERT INTO Ingredient (name, quantityTotal, unit) VALUES" +
		// 		"('Bread crumbs', 500000, 'g')," +
		// 		"('Butter', 500000, 'g')," +
		// 		"('Chocolate', 500000, 'g')," +
		// 		"('Chopped almonds', 500000, 'g')," +
		// 		"('Cinnamon', 500000, 'g')," +
		// 		"('Egg whites', 500000, 'ml')," +
		// 		"('Eggs', 500000, 'g')," +
		// 		"('Fine-ground nuts', 500000, 'g')," +
		// 		"('Flour', 500000, 'g')," +
		// 		"('Ground, roasted nuts', 500000, 'g')," +
		// 		"('Icing sugar', 500000, 'g')," +
		// 		"('Marzipan', 500000, 'g')," +
		// 		"('Potato starch', 500000, 'g')," +
		// 		"('Roasted, chopped nuts', 500000, 'g')," +
		// 		"('Sodium bicarbonate', 500000, 'g')," +
		// 		"('Sugar', 500000, 'g')," +
		// 		"('Vanilla sugar', 500000, 'g')," +
		// 		"('Vanilla', 500000, 'g')," +
		// 		"('Wheat flour', 500000, 'g');";
		// String q12 = "INSERT INTO Cookie (name) VALUES" +
		// 		"('Almond delight')," +
		// 		"('Amneris')," +
		// 		"('Berliner')," +
		// 		"('Nut cookie')," +
		// 		"('Nut ring')," +
		// 		"('Tango');";
		// String q13 = "INSERT INTO Recipes(amount, cookieName, ingredientName) VALUES" +
		// 		"(400, 'Almond delight', 'Butter')," +
		// 		"(279, 'Almond delight', 'Chopped almonds')," +
		// 		"(10, 'Almond delight', 'Cinnamon')," +
		// 		"(400, 'Almond delight', 'Flour')," +
		// 		"(270, 'Almond delight', 'Sugar')," +
		// 		"(250, 'Amneris', 'Butter')," +
		// 		"(250, 'Amneris', 'Eggs')," +
		// 		"(750, 'Amneris', 'Marzipan')," +
		// 		"(25, 'Amneris', 'Potato starch')," +
		// 		"(25, 'Amneris', 'Wheat flour')," +
		// 		"(250, 'Berliner', 'Butter')," +
		// 		"(50, 'Berliner', 'Chocolate')," +
		// 		"(50, 'Berliner', 'Eggs')," +
		// 		"(350, 'Berliner', 'Flour')," +
		// 		"(100, 'Berliner', 'Icing sugar')," +
		// 		"(5, 'Berliner', 'Vanilla sugar')," +
		// 		"(125, 'Nut cookie', 'Bread crumbs')," +
		// 		"(50, 'Nut cookie', 'Chocolate')," +
		// 		"(350, 'Nut cookie', 'Egg whites')," +
		// 		"(750, 'Nut cookie', 'Fine-ground nuts')," +
		// 		"(625, 'Nut cookie', 'Ground, roasted nuts')," +
		// 		"(375, 'Nut cookie', 'Sugar')," +
		// 		"(450, 'Nut ring', 'Butter')," +
		// 		"(450, 'Nut ring', 'Flour')," +
		// 		"(190, 'Nut ring', 'Icing sugar')," +
		// 		"(225, 'Nut ring', 'Roasted, chopped nuts')," +
		// 		"(200, 'Tango', 'Butter')," +
		// 		"(300, 'Tango', 'Flour')," +
		// 		"(4, 'Tango', 'Sodium bicarbonate')," +
		// 		"(250, 'Tango', 'Sugar')," +
		// 		"(2, 'Tango', 'Vanilla');";
		// try(Statement stmt = conn.createStatement()){
		// 	conn.setAutoCommit(false);
		// 	stmt.addBatch(q1);
		// 	stmt.addBatch(q2);
		// 	stmt.addBatch(q3);
		// 	stmt.addBatch(q4);
		// 	stmt.addBatch(q5);
		// 	stmt.addBatch(q6);
		// 	stmt.addBatch(q7);
		// 	stmt.addBatch(q8);
		// 	stmt.addBatch(q9);
		// 	stmt.addBatch(q10);
		// 	stmt.addBatch(q11);
		// 	stmt.addBatch(q12);
		// 	stmt.addBatch(q13);
		// 	stmt.executeBatch();
		// 	conn.commit();
		// 	conn.setAutoCommit(true);
		// 	return Jsonizer.anythingToJson("ok", "status");
		// } catch (SQLException e) {
		// 	e.printStackTrace();
		// 	try {
		// 		conn.rollback();
		// 	} catch (SQLException ex) {
		// 		ex.printStackTrace();
		// 	}
		// 	return Jsonizer.anythingToJson(e.getMessage(), "status");
		// }
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
