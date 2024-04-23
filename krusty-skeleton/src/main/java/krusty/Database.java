package krusty;

import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.sql.*;

import static krusty.Jsonizer.toJson;

public class Database {
	/**
	 * Modify it to fit your environment and then use this string when connecting to your database!
	 */
	private static final String jdbcString = "jdbc:mysql://localhost/krusty";

	// For use with MySQL or PostgreSQL
	private static final String jdbcUsername = "hbg29";
	private static final String jdbcPassword = "pqx717bq";

	private Connection conn;

	 /**
     * Create the database interface object. Connection to the database
     * is performed later.
     */
    public Database() {
        conn = null;
    }

	public void connect() {
		try {       	
        	// Use "jdbc:mysql://puccini.cs.lth.se/" + userName if you using our shared server
        	// If outside, this statement will hang until timeout.
            conn = DriverManager.getConnection 
                ("jdbc:mysql://puccini.cs.lth.se/hbg29", jdbcUsername, jdbcPassword);
        }
        catch (SQLException e) {
            System.err.println(e);
            e.printStackTrace();
        }
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

	public String getRawMaterials(Request req, Response res) {
		return "{}";
	}

	public String getCookies(Request req, Response res) {
		return "{\"cookies\":[]}";
	}

	public String getRecipes(Request req, Response res) {
		return "{}";
	}

	public String getPallets(Request req, Response res) {
		String sql = "SELECT p.palletID, p.cookieName, p.productionDate, wc.name AS customer_name, IF(p.isBlocked, 'yes', 'no') AS blocked FROM Pallets p JOIN Orders o ON p.orderID = o.orderID JOIN WholesaleCustomer wc ON o.customerID = wc.customerID Order by productionDate;";
       
		// ArrayList för att spara värden
        ArrayList<Object> values = new ArrayList<>();

        // Check and build SQL query dynamically based on query parameters
        if (req.queryParams("from") != null) {
            sql += " AND production_date >= ?";    				//
            values.add(req.queryParams("from"));
        }
        if (req.queryParams("to") != null) {		// kan va att de blir fel när man lägger till "AND" efter "Order By"
            sql += " AND production_date <= ?";
            values.add(req.queryParams("to"));
        }
        if (req.queryParams("cookie") != null) {
            sql += " AND cookie = ?";
            values.add(req.queryParams("cookie"));
        }
        if (req.queryParams("blocked") != null) {
            sql += " AND blocked_bool_attr = ?";
            values.add(req.queryParams("blocked").equals("yes")); // Convert "yes" to boolean
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set parameters safely				//kan man slänga sig med objects så eller måste de vara rätt typer typ string
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }

            // Execute query
            ResultSet rs = ps.executeQuery();

            // Process results
            StringBuilder response = new StringBuilder("{ \"pallets\": [ ");
            while (rs.next()) {
                response.append("{ \"id\": ").append(rs.getInt("id")).append(", \"cookie\": \"").append(rs.getString("cookie")).append("\", \"production_date\": \"").append(rs.getString("production_date")).append("\", \"customer\": \"").append(rs.getString("customer")).append("\", \"blocked\": \"").append(rs.getString("blocked")).append("\" }, ");
            }
            // Remove trailing comma and close array
            if (response.length() > 14) {
                response.delete(response.length() - 2, response.length());
            }
            response.append(" ] }");

            return response.toString();
        } catch (SQLException e) {
            // Handle exceptions
            e.printStackTrace();
            return "An error occurred";
        }
    }


		
		
	

	public String reset(Request req, Response res) {
		return "{}";
	}

	public String createPallet(Request req, Response res) {
		return "{}";
	}
}
