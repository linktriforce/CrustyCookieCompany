package krusty;

import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.sql.*;

import static krusty.Jsonizer.toJson;

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

	public String getCustomers(Request req, Response res) {
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

	public String getRecipes(Request req, Response res) {
		String sql = "select cookieName, ingredientName, amount, unit from Recipe\n" + //
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

	public String getPallets(Request req, Response res) {
		String sql = "select * from Pallets";

		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ResultSet rs = ps.executeQuery();
			String json = Jsonizer.toJson(rs, "pallets");
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{\"pallets\":[]}";
	}

	public String reset(Request req, Response res) {
	
		return "{}";
	}

	public String createPallet(Request req, Response res) {
		String cookieName = req.queryParams("cookieName");

		if (cookieName == null || cookieName.isEmpty()){
			res.status(400);
			return "Missing param: cookieName";
		}

		try {
			
			String sql = "INSERT INTO Pallets (cookieName) VALUES (?)";
			PreparedStatement  ps = conn.prepareStatement(sql);
			ps.setString(1, cookieName);
			ps.executeUpdate();
			ps.close();
			return "Created Pallet";
		} catch (SQLException e) {
			res.status(500);
			return "Failed to create pallet";
			// TODO: handle exception
		}
	}
}
