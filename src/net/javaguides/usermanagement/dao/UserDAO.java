package net.javaguides.usermanagement.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.javaguides.usermanagement.model.User;

/**
 * AbstractDAO.java This DAO class provides CRUD database operations for the
 * table users in the database.
 * 
 * Let's create a UserDAO class which is a Data Access Layer (DAO) class that provides CRUD mysql
 * 
 * (Create, Read, Update, Delete) operations for the table users in a database. 
 */

public class UserDAO {
    private String jdbcURL = "jdbc:mysql://localhost:3306/demo?useSSL=false";
	//private String jdbcURL = System.getenv("DATABASE_URL");
    //private String dburl = "postgres://bvgyhcdkjguuhg:1ba2d4ee6c61112a2a68dea115e3ae0e6af2849a932ad55788ecdee1853263fe@ec2-34-194-215-27.compute-1.amazonaws.com:5432/d6t5t2mmjga6sh";

    
    
    private String jdbcUsername = "root";
    private String jdbcPassword = "password";

    public static User users[] = new User[500];
    private static final String INSERT_USERS_SQL = "INSERT INTO users" + "  (name, email, country) VALUES " +
        " (?, ?, ?);";

    private static final String SELECT_USER_BY_ID = "select id,name,email,country from users where id =?";
    private static final String SELECT_ALL_USERS = "select * from users ORDER BY name ASC";
    private static final String SELECT_ALL_USERS_DESC = "select * from users ORDER BY name DESC";
    private static final String DELETE_USERS_SQL = "delete from users where id = ?;";
    private static final String UPDATE_USERS_SQL = "update users set name = ?,email= ?, country =? where id = ?;";

    public UserDAO() {}

    // Connect to the database using jdbc driver with url, name, and pass
    protected Connection getConnection() {
    	//String password = "";
    	//String username = "";

    	//System.out.println("JDBC SYS ENV = " + jdbcURL);
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); 
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
            //connection = DriverManager.getConnection(dburl);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
        	System.out.println("SQL Exception caught");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return connection;
    }
    
    
    public static void obtainUsersAPI() {
    	
    }
    

    public void insertUser(User user) throws SQLException {
        System.out.println(INSERT_USERS_SQL);
        // try-with-resource statement will auto close the connection.
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getCountry());
            System.out.println(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
        }
        
        /*
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL)) {
            for(int i = 0; i < 60; i++) {
            	System.out.println("Setting user from array");
            	//String name = users[i].getName();
            	preparedStatement.setString(1, users[i].getName());
                preparedStatement.setString(2, users[i].getEmail());
                preparedStatement.setString(3, users[i].getCountry());
                System.out.println(preparedStatement);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
        	System.out.println("We have found an error");
            printSQLException(e);
        }*/
    }

    public User selectUser(int id) {
        User user = null;
        // Step 1: Establishing a Connection
        try (Connection connection = getConnection();
            // Step 2:Create a statement using connection object
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID);) {
            preparedStatement.setInt(1, id);
            System.out.println(preparedStatement);
            // Step 3: Execute the query or update query
            ResultSet rs = preparedStatement.executeQuery();

            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String country = rs.getString("country");
                user = new User(id, name, email, country);
                
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return user;
    }

    public List < User > selectAllUsers(int x) {
    	String userSelection = SELECT_ALL_USERS;
    	if (x == 1) {
    		userSelection = SELECT_ALL_USERS_DESC;
    	}else {
    		userSelection = SELECT_ALL_USERS;
    	}
        // using try-with-resources to avoid closing resources (boiler plate code)
        List < User > users = new ArrayList < > ();
        // Step 1: Establishing a Connection
        try (Connection connection = getConnection();

            // Step 2:Create a statement using connection object
            PreparedStatement preparedStatement = connection.prepareStatement(userSelection);) {
            System.out.println(preparedStatement);
            // Step 3: Execute the query or update query
            ResultSet rs = preparedStatement.executeQuery();

            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String country = rs.getString("country");
                users.add(new User(id, name, email, country));
            }
        } catch (SQLException e) {
            printSQLException(e);
        }catch(NullPointerException v) {
        	v.printStackTrace();
        }
        return users;
    }

    public boolean deleteUser(int id) throws SQLException {
        boolean rowDeleted;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_USERS_SQL);) {
            statement.setInt(1, id);
            rowDeleted = statement.executeUpdate() > 0;
        }
        return rowDeleted;
    }

    public boolean updateUser(User user) throws SQLException {
        boolean rowUpdated;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE_USERS_SQL);) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getCountry());
            statement.setInt(4, user.getId());

            rowUpdated = statement.executeUpdate() > 0;
        }
        return rowUpdated;
    }

    private static void printSQLException(SQLException ex) {
        for (Throwable e: ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
    
	public static void myCSVReader(String filename) {

		String path = "C:\\Users\\AHorner\\Downloads\\" + filename;
		BufferedReader reader = null;
		String line = "";
		String csvSplitBy = ",";
		int count = 0;
		String fullName, email, country = "";
		int key = 0;

		//users[0] = new User("error", "error", "error");
		System.out.println("Starting csv reader");
		try {
			reader = new BufferedReader(new FileReader(path));
			while ((line = reader.readLine()) != null) {
				String[] s = line.split(csvSplitBy);
					if (s[0] != null) {
						System.out.println(s[0] + " " + s[1] + " " + s[2] + " " + s[3]);
					}
				
					String temp = s[0];
					//System.out.println(temp);
				//key = s[0];
				
				fullName = s[1] + " " + s[2];

				email = s[3];
				country = "USA";
				
				users[count] = new User(key, fullName, email, country);
				count++;

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		for (int i = count; i < count; i++) {

			if (users[i] == null) {
				users[i] = new User("error", "invalidemail@aol.com", "as");
			}
			try {
				// insertUser(user[i]);
			} catch (Exception e) {
				System.out.println("error error");
			}

			System.out.println(i + "= " + users[i].getName() + " " + users[i].getEmail() + " " + users[i].getCountry());

		}

	}
}