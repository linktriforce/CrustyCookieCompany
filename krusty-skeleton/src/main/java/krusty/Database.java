package krusty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

/**
 * 
 */
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

	/**
	 * Retrieves customers from the database
	 * 
	 * @author Albin Olausson
	 * @param req
	 * @param res
	 * @return customers in json format
	 * @throws SQLException
	 */
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

	/** 
	* @author Kristmann Thorsteinsson
	* @param req
	* @param res
	* @return Retrieves the name, amount and unit of raw materials
	* @throws SQLException
	* @throws JSONException
	*/
	public String getRawMaterials(Request req, Response res) throws SQLException, JSONException {
		String sql = "SELECT name, quantityTotal AS amount, unit FROM Ingredient";
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);

		JSONArray jsonArray = new JSONArray();
		while (resultSet.next()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", resultSet.getString("name"));
			jsonObject.put("amount", resultSet.getInt("amount"));
			jsonObject.put("unit", resultSet.getString("unit"));
			jsonArray.put(jsonObject);
		}

		JSONObject result = new JSONObject();
		result.put("raw-materials", jsonArray);

		return result.toString();
	}

	/**
	 * @author Kristmann Thorsteinsson
	 * @param req
	 * @param res
	 * @return Retrieves a list of cookies
	 * @throws SQLException
	 * @throws JSONException
	 */
	public String getCookies(Request req, Response res) throws SQLException, JSONException {
		String sql = "SELECT DISTINCT name FROM Cookie";
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);

		JSONArray jsonArray = new JSONArray();
		while (resultSet.next()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", resultSet.getString("name"));
			jsonArray.put(jsonObject);
		}
		JSONObject result = new JSONObject();
		result.put("cookies", jsonArray);

		return result.toString();
	}

	/**
	 * Retrieves the recipes from the database which produce 100 cookies
	 * @param req
	 * @param res
	 * @return 
	 * @throws SQLException
	 */
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

	/**
	 * Returns all produced pallets sorted by production date (newest first)
	 * @author Gustav Franzén
	 * @param req
	 * @param res
	 * @return
	 */
	public String getPallets(Request req, Response res) {

		String sql = "SELECT p.palletID AS id, p.cookieName AS cookie, p.productionDate AS production_date, "+
						"wc.name AS customer, IF(p.isBlocked, 'yes', 'no') AS blocked "+
					"FROM Pallets p "+
					"Left JOIN Orders o ON p.orderID = o.orderID "+
					"left JOIN WholesaleCustomer wc ON o.customerID = wc.customerID "+
					"WHERE 1=1 ";
       
        ArrayList<Object> values = new ArrayList<>();

        // Check and build SQL query dynamically based on query parameters
        if (req.queryParams("from") != null) {
            sql += " AND p.productionDate >= ?";    				
            values.add(req.queryParams("from"));
        }
        if (req.queryParams("to") != null) {
			sql += " AND p.productionDate <= ?";
            values.add(req.queryParams("to"));
        }
        if (req.queryParams("cookie") != null) {
            sql += " AND p.cookieName = ?";
            values.add(req.queryParams("cookie"));
        }
        if (req.queryParams("blocked") != null) {
            sql += " AND p.isBlocked = ?";
            values.add(req.queryParams("blocked").equals("yes")); // Convert "yes" to boolean
        }
		sql += " Order by p.productionDate";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set parameters safely
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }

            ResultSet rs = ps.executeQuery();

			String json = Jsonizer.toJson(rs, "pallets");
			return json;
            // Process results
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred";
        }
    }

	/** 
	* @Author Joachim Mohn
	* @param req
	* @param res
	* @return returns "ok" if database was reset, "error" if something went wrong
	*/
	public String reset(Request req, Response res) {
		
		String sqlFile = "initial-data.sql";

		try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
			StringBuilder queryBuilder = new StringBuilder();
			String line;
	
			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty() && !line.startsWith("--")) {
					queryBuilder.append(line);
	
					if (line.endsWith(";")) {
						String query = queryBuilder.toString();
						try (PreparedStatement ps = conn.prepareStatement(query)) {
							ps.executeUpdate();
						} catch (SQLException e) {
							e.printStackTrace();
							return Jsonizer.anythingToJson(e.getMessage(), "status");
						}
						// Reset stringbuilder for the next query
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

	/**
	 * Creates a pallet of chosen cookie by client from the website. 
	 * A pallet contains 5400 cookies (15 cookies per bag, 10 bags per box, 36 boxes per pallet)
	 * @author Albin Olausson
	 * @param req
	 * @param res
	 * @return ok and id och created pallet
	 * @throws SQLException
	 */
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
			if (palletInserted == 0) return "{\"status\": \"unknown cookie\"}";
	
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
			return "{\"status\": \"error\" }";
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
