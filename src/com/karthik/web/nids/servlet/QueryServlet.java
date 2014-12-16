package com.karthik.web.nids.servlet;

import java.io.*;
import java.sql.*;
import java.util.concurrent.Future;

import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;

public class QueryServlet extends HttpServlet { // JDK 6 and above only

	// The doGet() runs once per HTTP GET request to this servlet.
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Set the MIME type for the response message
		response.setContentType("text/html");
		// Get a output writer to write the response message into the network
		// socket
		PrintWriter out = response.getWriter();

		// Get the choice of the dropdown list
		String str_choice = request.getParameter("attacks");
		int choice;

		if (str_choice.equals("smurf"))
			choice = 0;
		else if (str_choice.equals("ipsweep"))
			choice = 1;
		else if (str_choice.equals("neptune"))
			choice = 2;
		else
			choice = 3;

		// Get the Rule corresponding to the attack
		Rule r = new Rule();
		RuleMaker rm = new RuleMaker();
		try {
			r = rm.makeRules(choice);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double fitness = r.getFitness();
		int src_bytes_lower = r.getSourceBytesLower();
		int src_bytes_upper = r.getSourceBytesUpper();
		int dst_bytes_lower = r.getDestBytesLower();
		int dst_bytes_upper = r.getDestBytesUpper();
		int count_lower = r.getCountLower();
		int count_upper = r.getCountUpper();
		int srv_count_lower = r.getSrvCountLower();
		int srv_count_upper = r.getSrvCountUpper();

		Connection conn = null;
		Statement stmt = null;
		try {
			// Step 1: Allocate a database Connection object
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (java.lang.ClassNotFoundException cfe) {
			}
			conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/nids", "root", "1234"); // <==
																			// Check!
			// database-URL(hostname, port, default database), username,
			// password

			// Step 2: Allocate a Statement object within the Connection
			stmt = conn.createStatement();

			// Step 3: Execute a SQL SELECT query
			String sqlStr = "select * from rules where attack_name = " + "'"
					+ request.getParameter("attacks") + "'";

			// Print an HTML page as the output of the query
			out.println("<html><head><title>Query Response</title></head><body>");
			out.println("<h3>Thank you for your query.</h3>");
			out.println("<p>You query is: " + sqlStr + "</p>"); // Echo for
																// debugging
			ResultSet rset = stmt.executeQuery(sqlStr); // Send the query to the
														// server

			// Step 4: Process the query result set
			int count = 0;
			while (rset.next()) {
				// Print a paragraph <p>...</p> for each record
				out.println("<p>" + rset.getString("attack_name") + ", "
						+ rset.getString("src_bytes_lower") + ", "
						+ rset.getString("src_bytes_upper") + ", "
						+ rset.getString("dst_bytes_lower") + ", "
						+ rset.getString("dst_bytes_upper") + ", " + "</p>");
				count++;
			}

			// ------------------- Memcached Implementation

			MemcachedClient memcachedClient = new MemcachedClient(
					AddrUtil.getAddresses("127.0.0.1:11211"));
			String rule = (String) memcachedClient.get(str_choice);

			if (rule == null) {
				// First make the rule object serializable
				String serial_rule = r.getSourceBytesLower() + ","
						+ r.getSourceBytesUpper() + "," + r.getFitness();

				Future<Boolean> result = memcachedClient.add(str_choice, 0,
						serial_rule);

			}

			rule = (String) memcachedClient.get(str_choice);

			System.out.println("The following are the rules of " + str_choice
					+ " stored in Memcache:" + rule);

			// ------------------- End Memcached Implementation

			out.println("<p>==== " + count + " records found =====</p><hr>");
			out.println("<br>Fitness Value:<input type=\"text\" id=\"fitness_value\"><br>");
			out.println("<script>document.getElementById(\"fitness_value\").value=\""
					+ Double.toString(fitness) + "\"</script>");
			out.println("<br>The parameters corresponding to the rule are:<br><br>");
			out.println("Source Bytes Range: &nbsp&nbsp  ");
			out.println("<input class=\"textbox\" type=\"text\" id=\"src_bytes_lower\">&nbsp&nbspto&nbsp&nbsp<input class=\"textbox\" type=\"text\" id=\"src_bytes_upper\"><br><br>");
			out.println("<script>document.getElementById(\"src_bytes_lower\").value=\""
					+ Integer.toString(src_bytes_lower) + "\"</script>");
			out.println("<script>document.getElementById(\"src_bytes_upper\").value=\""
					+ Integer.toString(src_bytes_upper) + "\"</script>");
			out.println("Destination Bytes Range:&nbsp&nbsp");
			out.println("<input class=\"textbox\" type=\"text\" id=\"dst_bytes_lower\">&nbsp&nbspto&nbsp&nbsp<input class=\"textbox\" type=\"text\" id=\"dst_bytes_upper\"><br><br>");
			out.println("<script>document.getElementById(\"dst_bytes_lower\").value=\""
					+ Integer.toString(dst_bytes_lower) + "\"</script>");
			out.println("<script>document.getElementById(\"dst_bytes_upper\").value=\""
					+ Integer.toString(dst_bytes_upper) + "\"</script>");
			out.println("Count Range:&nbsp&nbsp");
			out.println("<input class=\"textbox\" type=\"text\" id=\"count_lower\">&nbsp&nbspto&nbsp&nbsp<input class=\"textbox\" type=\"text\" id=\"count_upper\"><br><br>");

			out.println("<script>document.getElementById(\"count_lower\").value=\""
					+ Integer.toString(count_lower) + "\"</script>");
			out.println("<script>document.getElementById(\"count_upper\").value=\""
					+ Integer.toString(count_upper) + "\"</script>");
			out.println("Srv Count Range:&nbsp&nbsp");
			out.println("<input class=\"textbox\" type=\"text\" id=\"srv_count_lower\">&nbsp&nbspto&nbsp&nbsp<input class=\"textbox\" type=\"text\" id=\"srv_count_upper\"><br>"
					+ "<style>"
					+ " .textbox {"
					+ "  background: white;"

					+ "    border: 1px solid #FFFFFF;"
					+ "    border-radius: 5px;"
					+ "    box-shadow: 0 0 5px 3px #2B60DE;"
					+ "    color: #666;"
					+ "    outline: none;"
					+ "    height:23px;"
					+ "    width: 75px;"
					+ "  }"
					+ " </style>");

			out.println("<script>document.getElementById(\"srv_count_lower\").value=\""
					+ Integer.toString(srv_count_lower) + "\"</script>");
			out.println("<script>document.getElementById(\"srv_count_upper\").value=\""
					+ Integer.toString(srv_count_upper) + "\"</script>");

			out.println("</body></html>");
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			out.close(); // Close the output writer
			try {
				// Step 5: Close the resources
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
}
