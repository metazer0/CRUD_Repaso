import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.sql.*;


public class Main {

    public static String dbURL = "jdbc:mysql://localhost:3306/sampledb?user=root";
    public static String username = "root";
    public static String password = "root";
    public static void main(String[] args) {

        try(Connection connection = DriverManager.getConnection(dbURL,username,password);){
            if(connection!=null){
                System.out.println("Connected!");
                HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);
                server.createContext("/create-user", new CreateUserHandler());
                server.createContext("/", new StaticFileHandler());
                System.out.println("Server Running...");
                server.start();
            }
        }catch(SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        User user = new User(2,"Mark","mark123","Mark Smith","mark@smith.com");
//        insertUser(user,dbURL,username,password );
//        updateUser(1,"john456","newsecret123","John H. Smith", "johhnsmith@smith.com",dbURL,username,password);
//        System.out.println(getUserById(1,dbURL,username,password));
//        System.out.println(getUserById(2,dbURL,username,password));
    }

    static class StaticFileHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestURI = exchange.getRequestURI().toString();
            String filePath = "src" + requestURI;

            if (requestURI.equals("/") || requestURI.isEmpty()) {
                filePath += "index.html"; // Serve index.html as the default page
            }

            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                // Read the file content and send it as the response
                exchange.sendResponseHeaders(200, file.length());
                OutputStream os = exchange.getResponseBody();
                Files.copy(file.toPath(), os);
                os.close();
            } else {
                // File not found, send a 404 response
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    static class CreateUserHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(exchange.getRequestMethod().equals("POST")){
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();

                String userId = extractValue(formData,"userId");
                String userName = extractValue(formData,"username");
                String userpassword = extractValue(formData,"password");
                String fullname = extractValue(formData,"fullname");
                String email = extractValue(formData,"email");
                int user_id = Integer.parseInt(userId);
                fullname = URLDecoder.decode(fullname, "UTF-8");
                email = URLDecoder.decode(email, "UTF-8");
                insertUser(user_id,userName,userpassword,fullname,email,dbURL,username,password);
                String res = "user created successfully!";
                exchange.sendResponseHeaders(200,res.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(res.getBytes());
                os.close();
            }else{
                exchange.sendResponseHeaders(405,-1);
            }
        }

        private String extractValue(String formData, String fieldName){
            int start = formData.indexOf(fieldName + "=");
            if (start >= 0) {
                start+=fieldName.length()+1;
                int end = formData.indexOf("&",start);
                if(end < 0){
                    end = formData.length();
                }
                return formData.substring(start,end);
            }
            return null;
        }
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

    public static void insertUser(int userId,String newUserName,String newPassword, String newFullname, String newEmail, String dbURL, String username, String password) {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {

            String sql = "INSERT INTO users (username, password, fullname, email) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, newUserName);
            preparedStatement.setString(2, newPassword);
            preparedStatement.setString(3, newFullname);
            preparedStatement.setString(4, newEmail);

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

