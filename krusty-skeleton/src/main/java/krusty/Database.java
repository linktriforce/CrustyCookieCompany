package krusty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
		getCustomers(null, null);
	}

	// TODO: Implement and change output in all methods below!

	public String getCustomers(Request req, Response res) {
		//Detta är bara testkod. Kollade så att conenction till databasen fungerade vilken den nu gör!
		String sql = "select * from WholesaleCustomer";

		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString("name"));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "{}";
	}

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

	public String getRecipes(Request req, Response res) {
		return "{}";
	}

	public String getPallets(Request req, Response res) {
		return "{\"pallets\":[]}";
	}

	public String reset(Request req, Response res) {
		return "{}";
	}

	public String createPallet(Request req, Response res) {
		return "{}";
	}
}
