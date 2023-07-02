import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String dbURL = "jdbc:mysql://localhost:3306/sampledb?user=root";
        String username = "root";
        String password = "root";

        try(Connection connection = DriverManager.getConnection(dbURL,username,password);){
            if(connection!=null){
                System.out.println("Connected!");
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }

//        User user = new User(2,"Mark","mark123","Mark Smith","mark@smith.com");
//        insertUser(user,dbURL,username,password );
//        updateUser(1,"john456","newsecret123","John H. Smith", "johhnsmith@smith.com",dbURL,username,password);
//        System.out.println(getUserById(1,dbURL,username,password));
//        System.out.println(getUserById(2,dbURL,username,password));
    }

    public static User getUserById(int userId, String dbURL, String username, String password){

        try(Connection conn = DriverManager.getConnection(dbURL,username,password)){
            String query = "SELECT * FROM users where user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1,userId);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    User user = new User();
                    user.setUserId(resultSet.getInt("user_id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPassword(resultSet.getString("password"));
                    user.setFullname(resultSet.getString("fullname"));
                    user.setEmail(resultSet.getString("email"));
                    return user;
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void insertUser(User user, String dbURL, String username, String password) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {

            String sql = "INSERT INTO users (username, password, fullname, email) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getFullname());
            preparedStatement.setString(4, user.getEmail());

            int rowAffected = preparedStatement.executeUpdate();
            if(rowAffected > 0 ){
                System.out.println("Insertion successful!");
            }else{
                System.out.println("Insertion failed!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void updateUser(int userId,String newUserName,String newPassword, String newFullname, String newEmail ,String dbURL, String username, String password){


        try(Connection connection = DriverManager.getConnection(dbURL,username,password)){

            String query = "UPDATE users SET username = ?, password = ?, fullname = ?, email = ? WHERE user_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1,newUserName);
            preparedStatement.setString(2,newPassword);
            preparedStatement.setString(3,newFullname);
            preparedStatement.setString(4,newEmail);
            preparedStatement.setInt(5,userId);


            int rowAffected = preparedStatement.executeUpdate();
            if(rowAffected > 0 ){
                System.out.println("Update successful!");
            }else{
                System.out.println("Update failed!");
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

}

