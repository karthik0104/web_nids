package com.karthik.web.nids.servlet;

import java.io.*;
import java.sql.*;
import java.util.concurrent.Future;

import javax.servlet.*;
import javax.servlet.http.*;

import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;

public class QueryServlet extends HttpServlet { // JDK 6 and above only

	// The doGet() runs once per HTTP GET request to this servlet.
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		long start1, start2, end1, end2;

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

		// ------------------- CouchDB Implementation

		CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
		Response resp;

		// How to insert JSON already formatted

		JsonParser parser = new JsonParser();
		String json_text = "{\"_id\": \"a" + (int) (Math.random() * 100)
				+ "\",\"a\": \"A\"}";
		System.out.println(json_text);
		JsonObject o = (JsonObject) parser.parse(json_text);

		// Prepare JSON

		JsonObject object = new JsonObject();
		object.addProperty("attack_name", str_choice);
		object.addProperty("src_bytes_lower", new Integer(src_bytes_lower));
		object.addProperty("src_bytes_upper", new Double(src_bytes_upper));
		object.addProperty("fitness", new Double(fitness));
		object.addProperty("nickname", "Karthik");
		object.addProperty("_id", str_choice + Integer.toString((int) (Math.random() * 100000)));
		object.addProperty("rev", "3");

		try {
			resp = dbClient.save(object);
		} catch (org.lightcouch.DocumentConflictException e) {
			// if we insert something that already exists
			// we get Exception in thread "main"
			// org.lightcouch.DocumentConflictException: << Status: 409
			// (Conflict)
		}

		dbClient.shutdown();

		// ------------------- End CouchDB Implementation

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

		start2 = System.currentTimeMillis();
		rule = (String) memcachedClient.get(str_choice);
		end2 = System.currentTimeMillis();

		System.out.println("Memcached:" + (end2 - start2));

		System.out.println("The following are the rules of " + str_choice
				+ " stored in Memcache:" + rule);

		// ------------------- End Memcached Implementation

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
				+ "    width: 75px;" + "  }" + " </style>");

		out.println("<script>document.getElementById(\"srv_count_lower\").value=\""
				+ Integer.toString(srv_count_lower) + "\"</script>");
		out.println("<script>document.getElementById(\"srv_count_upper\").value=\""
				+ Integer.toString(srv_count_upper) + "\"</script>");

		out.println("</body></html>");

	}
}
