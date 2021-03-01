package net.javaguides.usermanagement.web;


import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.javaguides.usermanagement.dao.UserDAO;
import net.javaguides.usermanagement.model.User;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO userDAO;

	// Date is not correct, outputting the mont in REVERSE
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static String dateString = sdf.format(new Date());
	// declaring variables for ams360 API calls
	private static String amsAuthUrl = "https://api-sandbox.vertafore.com/oauth/clienttoken/issue/v1/?email=mnorman@insuranceexpress.com&instanceid=1023475-1";
	private static String empurl = "https://api-sandbox.vertafore.com/authgrant/v1/api/";
	private static String ams_access_token = "";
	// private static String clientId = "1000.NZBHBQZ3KHKTOT64QV2KPBJ0FSTYVV";
	// private static String clientSecret =
	// "7aaddf1df81a0928ac3750ffafd7d5d63fcfa3cc5c";
	// private static String encode = clientId + ":" + clientSecret;
	// private static String encodedUrl =
	// Base64.getEncoder().encodeToString(encode.getBytes());

	// Dclaring our http client and response
	private static OkHttpClient client = new OkHttpClient().newBuilder().build();
	private static Response response;
	private static Request request;

	public static String[] custId = new String[1500];
	private static String[] first_name = new String[1500];
	private static String[] last_name = new String[1500];

	private static int jsonCount = 0;
	private static String prettyJsonString = "";

	private static String responseBody = "";
	private static String[] expiryDate = null;

	private static String[] policyId = null;
	private static String[] effectiveDate = null;

	public void init() {
		userDAO = new UserDAO();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getServletPath();
		try {
			switch (action) {
			case "/new":
				showNewForm(request, response);
				break;
			case "/insert":
				insertUser(request, response);
				break;
			case "/delete":
				deleteUser(request, response);
				break;
			case "/edit":
				showEditForm(request, response);
				break;
			case "/update":
				updateUser(request, response);
				break;
			case "/chart":
				monthlyBreakdown(request, response);
				break;
			case "/listUserDesc":
				listUserDesc(request, response);
				break;
			case "/retained-chart":
				retainedChart(request, response);
				amsObtainToken();
				//String[] s = getAMSCustomers(); // Get all customers
				// getAMSEmployees(); // Commented this out because getAMSEmployees now returns
				// the json as a string and is set to a variable in "formatJson"
				//formatAMSCustomers(s);
				getCustomerPolicies();

				break;
			default:
				listUser(request, response);

				break;
			}
		} catch (SQLException e) {
			throw new ServletException();
		}
		// response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	public static void getCustomerPolicies() {
		client = new OkHttpClient().newBuilder().build();
		// for(int i = 0; i <3; i++) {
		System.out.println("getting policies for \"46d20382-a95b-409c-9fda-7c721510cee5\"");
		Request request = new Request.Builder()
				.url("https://api-sandbox.vertafore.com/authgrant/v1/api/Customers("
						+ /* custId[71] */ "46d20382-a95b-409c-9fda-7c721510cee5" + ")/Policies")
				.method("GET", null).addHeader("Authorization", "Bearer " + ams_access_token).build();
		try {
			response = client.newCall(request).execute();
			responseBody = response.body().string();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser parser = new JsonParser(); 
			JsonElement je = parser.parse(responseBody);
			prettyJsonString = gson.toJson(je);
			AMSCustPolicyInfo(prettyJsonString);
			// System.out.println(prettyJsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// }

	}

	public static String[] getAMSCustomers() {
		String[] stringArray = new String[3];
		client = new OkHttpClient().newBuilder().build();
		for (int i = 0; i < 250; i++) {
			request = new Request.Builder()
					.url("https://api-sandbox.vertafore.com/authgrant/v1/api/Customers" + "?$skip=" + i * 500)
					.method("GET", null).addHeader("Authorization", "Bearer " + ams_access_token).build();
			try {
				response = client.newCall(request).execute();
				responseBody = response.body().string();
				format(responseBody);
				stringArray[i] = prettyJsonString;

			} catch (Exception e) {
				e.printStackTrace();
			}
			// Break for loop for testing purposes only
			// Breaking after one iteration for testing purposes only
			if (i == 0) {
				break;
			}
		}
		return stringArray;
	}

	private static void AMSCustPolicyInfo(String s) throws ParseException {
		System.out.println("String s = " + s);
		String highest = "1990-08-13T00:00:00Z";
		String polIdToCancel = "";

		Date highestDate = new Date();
		Date date1 = new Date();
		Date date2 = new Date();
		JSONObject jsonObj = new JSONObject(s);

		// This line successfully obtains the value for amount of json values returned
		String count = jsonObj.getString("@odata.context");
		jsonCount = (int) jsonObj.get("@odata.count");

		// Creating an array of length from data count to request all policies infos
		// Changed these from local to global variables
		policyId = new String[jsonCount];
		effectiveDate = new String[jsonCount];
		expiryDate = new String[jsonCount];

		System.out.println("Data count from data context = " + count);
		System.out.println("Data count from data.count  = " + jsonCount);

		JSONArray jsonArray = (JSONArray) jsonObj.get("value");

		for (int i = 0; i < jsonCount; i++) {
			JSONObject json = (JSONObject) jsonArray.get(i);
			effectiveDate[i] = json.getString("EffectiveDate");
			policyId[i] = json.getString("PolicyId");
			expiryDate[i] = json.getString("ExpiryDate");

		}

		int expiredPoliciesCount = 0;

		// This array is used to store the index number for policies that we want to
		// cancel
		int cancelIndexArray[] = new int[500];

		String mostRecentDate = "1990-08-13T00:00:00Z";

		// Testing purposes only, iterate through the array and output effective / expiry date and policy id
		for (int i = 0; i < jsonCount; i++) {
			System.out.println(i + " Eff date = " + effectiveDate[i] + ", Pol Id =" + policyId[i] + " | Expiry date = "
					+ expiryDate[i]);
			Date now = null;
			
			try {

				now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(sdf.format(now));
				
				// Set current month for expiry comparison to one month ago
				now.setMonth(now.getMonth()-1);
				date1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(expiryDate[i]);
				highestDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(highest);
				if(i != jsonCount -1) {
				
					date2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(expiryDate[i + 1]);
				}else {
					date2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(expiryDate[i]);
				}				
				// If the cancellation date is before today, get the index and store it in a new
				// array
				if (date1.before(now)) {
					if (i == jsonCount - 2) {
						if (date2.before(now)) {
							cancelIndexArray[expiredPoliciesCount] = i;
							expiredPoliciesCount++;
							System.out.println("Expired policy count increment inside loop");
						}
					}else {
						cancelIndexArray[expiredPoliciesCount] = i;
						expiredPoliciesCount++;
						System.out.println("Expired policy count increment");
					}

				}
				// if(date1.before(now) && date2.before(now) ) {
				if (date1.compareTo(date2) > 0) {
					if (date1.compareTo(highestDate) > 0) {
						if (now.after(date1)) { // Testing to see if date is before today
							highestDate = date1;
							System.out.println("i is after i+1");
							highest = expiryDate[i];
							mostRecentDate = expiryDate[i];
							polIdToCancel = policyId[i];
						}
					}

				} else if (date2.compareTo(date1) > 0) {
					if (date2.compareTo(highestDate) > 0) {
						if (now.after(date2)) { // Testing to see if date is before today

							System.out.println("i is before i+1");
							mostRecentDate = expiryDate[i + 1];
							highestDate = date2;
							highest = expiryDate[i + 1];
							polIdToCancel = policyId[i];
						}
					}

				} else if (date1.compareTo(date2) == 0) {
					System.out.println("i is equal to i+1");
					if(date1.before(now)) {
						highestDate = date1;
						System.out.println("i is after i+1");
						highest = expiryDate[i];
						mostRecentDate = expiryDate[i];
						polIdToCancel = policyId[i];
					}
					
					//expiredPoliciesCount++;
				}
				// }

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");
			System.out.println("Count of expired policies: " + expiredPoliciesCount);
			System.out.println("End comparison\nMost recent expiry date is " + mostRecentDate);
			System.out.println("String highest = " + highest);
			System.out.println("highestDate = " + highestDate);
			System.out.println("Now time = " + now);
			System.out.println("Policy id for most recent policy: " + polIdToCancel);
			


		} // End for loop for finding expired dates

		CancellationPolicy cancelledPolicies[] = new CancellationPolicy[expiredPoliciesCount];
		for (int i = 0; i < expiredPoliciesCount; i++) {
			cancelledPolicies[i] = new CancellationPolicy(effectiveDate[cancelIndexArray[i]],
					expiryDate[cancelIndexArray[i]], policyId[cancelIndexArray[i]]);

			System.out.println("Index to cancel" + cancelIndexArray[i] + " | Policy id = "
					+ cancelledPolicies[i].getId() + " | Expire date: " + cancelledPolicies[i].getExpiredDate()
					+ " | Effective date = " + cancelledPolicies[i].getEffectiveDate());
			getPolicyTransactions(cancelledPolicies[i].getEffectiveDate(), cancelledPolicies[i].getId(),cancelledPolicies[i].getExpiredDate() );

			System.out.println(
					"------------------------------------------------------------------------------------------------------------------------------------------------");

		}


	}

	private static String format(String responseBody) {
		System.out.println("Enter format()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser parser = new JsonParser();
		JsonElement je = parser.parse(responseBody);
		prettyJsonString = gson.toJson(je);
		responseBody = prettyJsonString;
		// System.out.println("Response body = " + prettyJsonString);
		return prettyJsonString;
	}

	private static void getPolicyTransactions(String effDate, String polId, String expDate) {
		System.out.println("Getting Policy Transactions");
		client = new OkHttpClient().newBuilder().build();
		request = new Request.Builder()
				.url("https://api-sandbox.vertafore.com/authgrant/v1/api/PolicyTransactions(EffectiveDate=" + effDate
						+ ",PolicyId=" + polId + ")")
				.method("GET", null).addHeader("Authorization", "Bearer " + ams_access_token).build();
		try {
			response = client.newCall(request).execute();
			responseBody = response.body().string();
			String builder = format(responseBody);
			buildJsonUpdate(builder, effDate, polId, expDate);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	private static void buildJsonUpdate(String builder, String effDate, String polId, String expDate) {
		System.out.println("Entered build json update with builder = " + builder);
		JSONObject jsonObj = new JSONObject(builder);
		String billMethodPolicyTransaction = jsonObj.getString("BillMethodPolicyTransaction");
		String description = jsonObj.getString("Description");
		String enteredDate = jsonObj.getString("EnteredDate");
		
		// Policy expiry Date must be after policy effective date
		try {
			Date dateExpired = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(expDate);
			Date dateEffective = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(effDate);

			System.out.println("Exp Date = " + dateExpired);
			System.out.println("Eff date  = " + dateEffective);
			if(dateExpired.before(dateEffective) || dateExpired.equals(dateEffective)) {
				System.out.println("Same date for effective and expired");
				dateExpired.setMonth(dateExpired.getMonth()+1);
				//dateEffective.setMonth(dateEffective.getMonth()+1);
				System.out.println("New date expired = " + dateExpired);
				String newExpDate = sdf.format(dateExpired); //351cab6b-e589-4378-939d-f00c455236e5
				expDate = newExpDate;
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		String reasonPolicyTransaction = jsonObj.getString("ReasonPolicyTransaction");
		if(reasonPolicyTransaction.equals("") || reasonPolicyTransaction.equals(null)) {
			reasonPolicyTransaction = null;
		}
		String transType = jsonObj.getString("TransactionType");
		String src = jsonObj.getString("Source");
		String srcDescription = jsonObj.getString("SourceDescription");
		
		//String estRevenuePercent = jsonObj.getString("EstRevenuePercent");
		

		System.out.println("effective date = " + effDate);
		System.out.println("Policy Id  = " + polId);
		System.out.println("Bill method  = " + billMethodPolicyTransaction);
		System.out.println("Description = " + description);
		System.out.println("Entered Date  = " + enteredDate);
		System.out.println("Transaction type  = " + transType);

		System.out.println("--------------------------------------");
		
		/*
		try {
			Date entDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(enteredDate);
			//Date newDate = DateUtils.addMonths(entDate, 1);
			//System.out.println("entDate = " + entDate + "\newDate = " + newDate);
			//entDate = DateUtils.addMonths(entDate, 1);
			System.out.println("Updated ent date = " + entDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			
			System.out.println("This did not work");
			e.printStackTrace();
		}
		//String dateString = sdf.format(enteredDate);
		*/

		JSONObject json = new JSONObject();
		json.put("BillMethodPolicyTransaction", billMethodPolicyTransaction);
		json.put("Description", "Cancellation through API ");
		json.put("EffectiveDate", effDate);
		json.put("EnteredDate", enteredDate);
		json.put("PolicyId", polId);
		json.put("ReasonPolicyTransaction", reasonPolicyTransaction);
		json.put("Source", src);
		json.put("SourceDescription", srcDescription);
		json.put("TransactionType", "XLC");
		//json.put("EstRevenuePercent", estRevenuePercent);	

		//for(int i = 0; i < 2; i++) {
			client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/json");
			//if(i == 0) {
				//json.put("Description", "Update to XLN - Cancel Request");
				//json.put("TransactionType", "XLN");								
			//}else {
				//json.put("Description", "Update to XLC - Cancel Confirmation");
				//json.put("TransactionType", "XLC");	
			//}
			String jsonString = json.toString();
			System.out.println("Json String = " + jsonString);
			RequestBody body = RequestBody.create(mediaType, jsonString);
			Request request = new Request.Builder()
					  .url("https://api-sandbox.vertafore.com/authgrant/v1/api/PolicyTransactions(EffectiveDate=" + effDate +",PolicyId=" + polId +")")
					  .method("PUT", body)
					  .addHeader("Authorization", "Bearer " + ams_access_token)
					  .addHeader("Content-Type", "application/json")
					  .build();
			try {
				Response response = client.newCall(request).execute();
				responseBody = response.body().string();
				
				System.out.println("Response body = " + responseBody);
				System.out.println("__________________________________________________________________");
				
			}catch (Exception e) {
				e.printStackTrace();
			}
			
					
		//}
		
		// Need to create some counter that checks if we are making our first call to
		// update policy, than transtype
		// is "XLN", and if its the second time we are calling to update, transtype is
		// "XLC"

		

		//System.out.println("Json String = " + jsonString);

	}

	private static void formatAMSCustomers(String[] s) {

		int count = 0;
		int count2 = 0;

		// Changing iteration from < 1158 to < 500 for testing purposes
		for (int i = 0; i < 500; i++) {
			if (i < 500) {
				JSONObject jsonObj = new JSONObject(s[0]);
				jsonCount = (int) jsonObj.get("@odata.count");

				JSONArray jsonArray = (JSONArray) jsonObj.get("value");
				JSONObject json = (JSONObject) jsonArray.get(i);
				custId[i] = json.getString("CustomerId");
				first_name[i] = json.getString("FirstName");
				last_name[i] = json.getString("Last");
			} else if (i >= 500 && i < 1000) {
				JSONObject jsonObj = new JSONObject(s[1]);
				JSONArray jsonArray = (JSONArray) jsonObj.get("value");
				JSONObject json = (JSONObject) jsonArray.get(count);
				custId[i] = json.getString("CustomerId");
				first_name[i] = json.getString("FirstName");
				last_name[i] = json.getString("Last");
				count++;
			} else if (i >= 1000) {
				JSONObject jsonObj = new JSONObject(s[2]);
				JSONArray jsonArray = (JSONArray) jsonObj.get("value");
				JSONObject json = (JSONObject) jsonArray.get(count2);
				custId[i] = json.getString("CustomerId");
				first_name[i] = json.getString("FirstName");
				last_name[i] = json.getString("Last");
				count2++;
			}

		}
		// Going to need this value to iterate through all records
		System.out.println("odata count for AMS customers = " + jsonCount);
		// Changed from < 1158 to < 500 for testing purposes
		for (int i = 0; i < 500; i++) {
			if (custId[i].equals("") || custId[i] == null) {

			} else {
				System.out.println(i + ": " + "Customer id= " + custId[i] + " | First name = " + first_name[i]
						+ " | Last Name = " + last_name[i]);
			}

		}

	}

	public static void parseJson(String[] s) {
		String array1 = s[0];
		String array2 = s[1];
		// JSONObject jsonObj = new JSONObject(s[0]);
		// JSONArray jsonArray = (JSONArray) jsonObj.get("value");

		String[] email = new String[1500];
		String[] first_name = new String[1500];
		String[] last_name = new String[1500];
		int count = 0;
		int count2 = 0;
		for (int i = 0; i < 1158; i++) {
			if (i < 500) {
				JSONObject jsonObj = new JSONObject(s[0]);
				JSONArray jsonArray = (JSONArray) jsonObj.get("value");
				JSONObject json = (JSONObject) jsonArray.get(i);
				email[i] = json.getString("EMail");
				first_name[i] = json.getString("FirstName");
				last_name[i] = json.getString("LastName");
			} else if (i >= 500 && i < 1000) {
				JSONObject jsonObj = new JSONObject(s[1]);
				JSONArray jsonArray = (JSONArray) jsonObj.get("value");
				JSONObject json = (JSONObject) jsonArray.get(count);
				email[i] = json.getString("EMail");
				first_name[i] = json.getString("FirstName");
				last_name[i] = json.getString("LastName");
				count++;
			} else if (i >= 1000) {
				JSONObject jsonObj = new JSONObject(s[2]);
				JSONArray jsonArray = (JSONArray) jsonObj.get("value");
				JSONObject json = (JSONObject) jsonArray.get(count2);
				email[i] = json.getString("EMail");
				first_name[i] = json.getString("FirstName");
				last_name[i] = json.getString("LastName");
				count2++;
			}

		}

		for (int i = 0; i < 1158; i++) {
			if ((email[i].equals("") || email[i] == null) && (first_name[i].equals("") || first_name[i] == null)) {

			} else {
				System.out.println(
						i + ": " + email[i] + " | First name: " + first_name[i] + " | Last name: " + last_name[i]);
			}

		}
		// System.out.println("array 2 = \n" + array2 + "\n__________________");

	}

	public static String getAMSEmployees() {
		String prettyJsonStringArray[] = new String[3];


		client = new OkHttpClient().newBuilder().build();
		for (int i = 0; i < 3; i++) {

			request = new Request.Builder()
					.url("https://api-sandbox.vertafore.com/authgrant/v1/api/Employees?$skip=" + i * 500)
					.method("GET", null).addHeader("Authorization", "Bearer " + ams_access_token).build();
			try {
				Response response = client.newCall(request).execute();
			
				responseBody = response.body().string();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser parser = new JsonParser();
				JsonElement je = parser.parse(responseBody);
				prettyJsonString = gson.toJson(je);
				prettyJsonStringArray[i] = prettyJsonString;
				JSONObject json = new JSONObject(prettyJsonString);
				System.out.println("Count = " + i);
				System.out.println(prettyJsonString);

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}
		System.out.println("array length = " + prettyJsonStringArray.length);
		parseJson(prettyJsonStringArray);
		return "";
	}

	public static void amsObtainToken() {
		
		client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		RequestBody body = RequestBody.create(mediaType, "");
		request = new Request.Builder().url(amsAuthUrl).method("POST", body)
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.addHeader("Authorization",
						"Bearer NjNkNDllNTYtMjFmNC00MjJlLWEzZTMtZGFkYzEwMWY3YWZlOjg2NzIzYjU5LTAzODktNDJiMi05YTVjLTNmNmVmYTUzZWUzYw==")
				.addHeader("Access-Control-Allow-Origin", "true").build();
		try {
			response = client.newCall(request).execute();
			responseBody = response.body().string();
			JSONObject jsonObj = new JSONObject(responseBody);
			System.out.println("\n" + responseBody);
			String ams_refresh_token = jsonObj.getString("refresh_token");
			ams_access_token = jsonObj.getString("access_token");
			System.out.println("AMS Refresh Token: " + ams_refresh_token);
			System.out.println("AMS Access Token: " + ams_access_token);
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private void listUser(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException, ServletException {
		List<User> listUser = userDAO.selectAllUsers(0);
		request.setAttribute("listUser", listUser);
		RequestDispatcher dispatcher = request.getRequestDispatcher("user-list.jsp");
		dispatcher.forward(request, response);
	}

	private void listUserDesc(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException, ServletException {
		List<User> listUser = userDAO.selectAllUsers(1);
		request.setAttribute("listUser", listUser);
		RequestDispatcher dispatcher = request.getRequestDispatcher("listUserDesc.jsp");
		dispatcher.forward(request, response);
	}

	private void monthlyBreakdown(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException, ServletException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("monthly-breakdown.jsp");
		dispatcher.forward(request, response);
	}

	private void retainedChart(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException, ServletException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("retained-chart.jsp");
		dispatcher.forward(request, response);
	}

	private void showEditForm(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException {
		int id = Integer.parseInt(request.getParameter("id"));
		User existingUser = userDAO.selectUser(id);
		RequestDispatcher dispatcher = request.getRequestDispatcher("user-form.jsp");
		request.setAttribute("user", existingUser);
		dispatcher.forward(request, response);

	}

	private void showNewForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// UserDAO.myCSVReader("Employee-Information-Export.csv");
		RequestDispatcher dispatcher = request.getRequestDispatcher("user-form.jsp");
		dispatcher.forward(request, response);
	}

	private void insertUser(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String country = request.getParameter("country");
		User newUser = new User(name, email, country);
		userDAO.insertUser(newUser);
		response.sendRedirect("list");
	}

	private void updateUser(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		int id = Integer.parseInt(request.getParameter("id"));
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String country = request.getParameter("country");

		User book = new User(id, name, email, country);
		userDAO.updateUser(book);
		response.sendRedirect("list");
	}

	private void deleteUser(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		int id = Integer.parseInt(request.getParameter("id"));
		// System.out.println("");
		userDAO.deleteUser(id);
		response.sendRedirect("list");

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
