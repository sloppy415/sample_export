package main;

import java.beans.XMLDecoder;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import com.teamlazerbeez.crm.sf.soap.ConnectionBundle;
import com.teamlazerbeez.crm.sf.soap.ConnectionPool;
import com.teamlazerbeez.crm.sf.soap.ConnectionPoolImpl;
import com.teamlazerbeez.crm.sf.soap.PartnerConnection;
import com.teamlazerbeez.crm.sf.soap.PartnerQueryResult;
import com.teamlazerbeez.crm.sf.soap.PartnerSObject;

public class TestOracleClob {

	private static final String USER_AGENT = "Mozilla/5.0";

	private static final String URL = "http://localhost/request-for-information/handle";
	//private static final String URL = "http://eai-dev.ggu.edu/request-for-information/handle";

	private static final String username = "";
	private static final String password = "";
	private static final Integer orgId = 1;
	private static final Integer maxConcurrentApiCalls = 5;

	private static ConnectionPool<Integer> pool = null;

	public static void main(String[] args) {

		createConnectionPool();

		ConnectionBundle bundle = pool.getConnectionBundle(orgId);

		PartnerConnection partnerConn = bundle.getPartnerConnection();

		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:dis", "user1", "");

			String sqll = "select properties from error where id > 3593 order by id";

			Statement stmt = con.createStatement();

			ResultSet rss = stmt.executeQuery(sqll);
			
			System.out.println("SQL: "+sqll);

			while (rss.next()) {
				oracle.sql.CLOB clob = (oracle.sql.CLOB) rss.getClob("properties");

				if (clob != null) {

					String content = "";

					Reader is = clob.getCharacterStream();
					BufferedReader br = new BufferedReader(is);
					String s = br.readLine();
					while (s != null) {
						content += s;
						s = br.readLine();
					}

					ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes());
					XMLDecoder decoder = new XMLDecoder(stream);
					HashMap<String, String> properties = (HashMap<String, String>) decoder.readObject();

					/*
					 * String source = trunc(properties.get("source")); String
					 * channel = trunc(properties.get("channel"));
					 * 
					 * String sql =
					 * "SELECT Id FROM Campaign where (Source__c = '" + source +
					 * "' and Channel__c = '" + channel + "') LIMIT 1";
					 * System.out.println("SQL: " + sql); PartnerQueryResult qr
					 * = partnerConn.query(sql); if (qr.getSObjects().size() >
					 * 0) { PartnerSObject o = qr.getSObjects().get(0); //
					 * System.out.println(String.format("ID=%s, GGU_ID__c=%s",
					 * // o.getId(), o.getField("GGU_ID__c")));
					 * System.out.println(String.format("ID=%s", o.getId())); }
					 * else System.out.println(String.format(
					 * "Unable to find campaign by source=%s and channel=%s",
					 * source, channel));
					 */

					System.out.println("properties:" + properties);

					String urlParameter = "";

					urlParameter += "contactType=Prospect&";
					urlParameter += "firstName=" + trunc(properties.get("firstName"));
					urlParameter += "&middleName=" + trunc(properties.get("middleName"));
					urlParameter += "&lastName=" + trunc(properties.get("lastName"));
					urlParameter += "&email=" + trunc(properties.get("email"));
					urlParameter += "&source=" + trunc(properties.get("source"));
					urlParameter += "&channel=" + trunc(properties.get("channel"));
					urlParameter += "&address=" + trunc(properties.get("address"));
					urlParameter += "&city=" + trunc(properties.get("city"));
					urlParameter += "&state=" + trunc(properties.get("state"));
					urlParameter += "&zipcode=" + trunc(properties.get("zipcode"));
					urlParameter += "&country=" + trunc(properties.get("country"));
					urlParameter += "&mobilePhone="
							+ or(trunc(properties.get("mobilePhone")), trunc(properties.get("dayPhone")),
									trunc(properties.get("weekendPhone")), trunc(properties.get("eveningPhone")));
					urlParameter += "&homePhone=" + trunc(properties.get("homePhone"));
					urlParameter += "&workPhone=" + trunc(properties.get("workPhone"));
					urlParameter += "&workPhoneExtension=" + trunc(properties.get("workPhoneExtension"));
					urlParameter += "&internationalPhone=" + trunc(properties.get("internationalPhone"));
					urlParameter += "&levelOfInterest=" + trunc(properties.get("levelOfInterest"));
					urlParameter += "&areaOfInterest=" + trunc(properties.get("areaOfInterest"));
					urlParameter += "&programOfInterest=" + trunc(properties.get("programOfInterest"));
					urlParameter += "&gender=" + trunc(properties.get("gender"));
					urlParameter += "&dob=" + trunc(properties.get("dob"));
					urlParameter += "&mBranch=" + trunc(properties.get("mBranch"));
					urlParameter += "&mStatus=" + trunc(properties.get("mStatus"));
					urlParameter += "&stage=" + trunc(properties.get("stage"));
					urlParameter += "&bestCallTime=" + trunc(properties.get("bestCallTime"));
					urlParameter += "&preferredPhone=" + trunc(properties.get("preferredPhone"));
					urlParameter += "&currentStatus=" + trunc(properties.get("currentStatus"));
					urlParameter += "&referrerExtoleCode=" + trunc(properties.get("referrerExtoleCode"));
					urlParameter += "&referrerEmailAddress=" + trunc(properties.get("referrerEmailAddress"));
					urlParameter += "&referrerFirstName=" + trunc(properties.get("referrerFirstName"));
					urlParameter += "&referrerLastName=" + trunc(properties.get("referrerLastName"));
					urlParameter += "&referrerExtoleSource=" + trunc(properties.get("referrerExtoleSource"));
					urlParameter += "&attendTimeFrame=" + trunc(properties.get("attendTimeFrame"));
					urlParameter += "&educationLevel=" + trunc(properties.get("educationLevel"));
					urlParameter += "&gguSite=" + trunc(properties.get("gguSite"));
					urlParameter += "&internationalStudent=" + yesNo(trunc(properties.get("internationalStudent")));
					urlParameter += "&workExperience=" + trunc(properties.get("workExperience"));
					urlParameter += "&intendedLevel=" + trunc(properties.get("intendedLevel"));

					sendPost(URL, urlParameter);

					decoder.close();

					Thread.sleep(5000);

				}
			}

			rss.close();
			con.close();
		} catch (Exception e) {
			System.out.println("Error:" + e);
			e.printStackTrace();
		}
	}

	private static void sendPost(String url, String urlParameters) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());

	}

	private static String trunc(String val) {
		if (val != null)
			return val;
		return "";
	}

	private static String or(String phone1, String phone2, String phone3, String phone4) {
		if (phone1.length() > 0)
			return phone1;
		if (phone2.length() > 0)
			return phone2;
		if (phone3.length() > 0)
			return phone3;
		if (phone4.length() > 0)
			return phone4;

		return "";
	}

	private static String yesNo(String val) {
		if (val.toLowerCase().startsWith("yes"))
			return "yes";
		else
			return "no";
	}

	private static void createConnectionPool() {
		pool = new ConnectionPoolImpl<Integer>("ggu");

		pool.configureOrg(orgId, username, password, maxConcurrentApiCalls);

	}
}
