<%@ page import="java.sql.*" %>
<%
    String id = request.getParameter("id");
    boolean isAvailable = true;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/capstone", "root", "1234");
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM user_info WHERE id = ?");
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            isAvailable = false;
        }

        rs.close();
        stmt.close();
        conn.close();
    } catch (Exception e) {
        isAvailable = false;
    }

    out.print(isAvailable ? "ok" : "duplicate");
%>