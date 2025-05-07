<%@ page import="java.sql.*" contentType="text/html;charset=UTF-8" %>
<%
    String nickname = request.getParameter("nickname");
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/capstone", "root", "1234");

        String sql = "SELECT nickname FROM user_info WHERE nickname = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nickname);
        rs = pstmt.executeQuery();

        if (rs.next()) {
            out.print("duplicate");
        } else {
            out.print("ok");
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.print("error");
    } finally {
        if (rs != null) rs.close();
        if (pstmt != null) pstmt.close();
        if (conn != null) conn.close();
    }
%>
