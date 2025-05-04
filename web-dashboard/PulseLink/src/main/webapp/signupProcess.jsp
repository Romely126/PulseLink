<%@ page import="java.sql.*" %>
<%
    request.setCharacterEncoding("UTF-8");

    String id = request.getParameter("id");
    String password = request.getParameter("password");
    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String gender = request.getParameter("gender");
    String birth = request.getParameter("birth");

    // 전화번호 조합
    String phonePrefix = request.getParameter("phonePrefix");
    String phoneMid = request.getParameter("phoneMid");
    String phoneEnd = request.getParameter("phoneEnd");
    String phoneNum = phonePrefix + "-" + phoneMid + "-" + phoneEnd;

    // 주소
    String post = request.getParameter("post");
    String address = request.getParameter("address");
    String detailAddress = request.getParameter("detailAddress");
    String fullAddress = address + " " + detailAddress;

    Connection conn = null;
    PreparedStatement pstmt = null;

    String dbURL = "jdbc:mysql://localhost:3306/user_db";
    String dbUser = "root";
    String dbPass = "1234";

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(dbURL, dbUser, dbPass);

        String sql = "INSERT INTO user_info (id, password, name, email, sex, birthday, phoneNum, postNum, address) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, password);
        pstmt.setString(3, name);
        pstmt.setString(4, email);
        pstmt.setString(5, gender);
        pstmt.setString(6, birth);
        pstmt.setString(7, phoneNum);
        pstmt.setString(8, post);
        pstmt.setString(9, fullAddress);

        int result = pstmt.executeUpdate();

        if (result > 0) {
            out.println("<script>alert('회원가입 성공!'); location.href='signup.jsp';</script>");
        } else {
            out.println("<script>alert('회원가입 실패...'); history.back();</script>");
        }
    } catch(Exception e) {
        e.printStackTrace();
        out.println("<script>alert('에러 발생: " + e.getMessage() + "'); history.back();</script>");
    } finally {
        if (pstmt != null) try { pstmt.close(); } catch (Exception e) {}
        if (conn != null) try { conn.close(); } catch (Exception e) {}
    }
%>
