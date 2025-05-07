
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.sql.Connection, java.sql.DriverManager, java.sql.PreparedStatement, java.sql.SQLException" %>
<%
    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");

    // 파라미터 수신
    String id = request.getParameter("id");
    String password = request.getParameter("password");
    String confirmPassword = request.getParameter("confirmPassword");
    String nickname = request.getParameter("nickname");
    String name = request.getParameter("name");
    String gender = request.getParameter("gender");
    String birth = request.getParameter("birth");

    String emailId = request.getParameter("emailId");
    String emailDomain = request.getParameter("emailDomain");
    String email = emailId + "@" + emailDomain;

    String phonePrefix = request.getParameter("phonePrefix");
    String phoneMid = request.getParameter("phoneMid");
    String phoneEnd = request.getParameter("phoneEnd");
    String phoneNum = phonePrefix + phoneMid + phoneEnd;

    String post = request.getParameter("post");
    String address = request.getParameter("address");
    String detailAddress = request.getParameter("detailAddress");
    String fullAddress = address + " " + detailAddress;

    // === 서버 유효성 검사 시작 ===
   	if (id == null || id.trim().isEmpty() || !id.matches("^[a-zA-Z0-9]{5,}$")) {
    	out.println("<script>alert('아이디는 5자 이상, 영문 및 숫자만 입력 가능하며, 공백을 포함할 수 없습니다.'); history.back();</script>");
    	return;
	}


    if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
        out.println("<script>alert('비밀번호가 일치하지 않거나 누락되었습니다.'); history.back();</script>");
        return;
    }

    if (password.length() < 6 || password.contains(" ") || !password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
        out.println("<script>alert('비밀번호는 6자 이상, 특수문자 1개 포함, 공백 없이 입력해야 합니다.'); history.back();</script>");
        return;
    }

    // 비밀번호 강도 검사
    int strength = 0;
    if (password.matches(".*[a-z].*")) strength++;
    if (password.matches(".*[A-Z].*")) strength++;
    if (password.matches(".*[0-9].*")) strength++;
    if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength++;

    if (strength <= 1) {
        out.println("<script>alert('비밀번호 보안 수준이 너무 약합니다.'); history.back();</script>");
        return;
    }

    if (nickname == null || nickname.length() < 1 || nickname.length() > 8 || nickname.contains(" ")) {
        out.println("<script>alert('닉네임은 1~8자 이내로 공백 없이 입력해야 합니다.'); history.back();</script>");
        return;
    }

    if (name == null || name.length() < 2 || name.contains(" ")) {
        out.println("<script>alert('이름은 2자 이상 공백 없이 입력해야 합니다.'); history.back();</script>");
        return;
    }

    if (gender == null || (!gender.equals("남성") && !gender.equals("여성"))) {
        out.println("<script>alert('성별을 선택하세요.'); history.back();</script>");
        return;
    }

    if (birth == null || birth.trim().equals("")) {
        out.println("<script>alert('생년월일을 입력하세요.'); history.back();</script>");
        return;
    }

    if (phoneNum == null || !phoneNum.matches("\\d{9,11}")) {
        out.println("<script>alert('전화번호는 숫자만 입력해야 합니다.'); history.back();</script>");
        return;
    }

    if (emailId == null || emailDomain == null || !email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
        out.println("<script>alert('이메일 형식이 올바르지 않습니다.'); history.back();</script>");
        return;
    }

    if (address == null || address.trim().equals("")) {
        out.println("<script>alert('기본 주소를 입력하세요.'); history.back();</script>");
        return;
    }

    // === DB 연결 및 INSERT ===
    Connection conn = null;
    PreparedStatement pstmt = null;

    String dbURL = "jdbc:mysql://localhost:3306/capstone";
    String dbUser = "root";
    String dbPass = "1234";

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(dbURL, dbUser, dbPass);

        String sql = "INSERT INTO user_info (id, password, name, nickname, email, sex, birthday, phoneNum, postNum, address) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, password); // 배포 시 SHA-256 등으로 해싱 권장
        pstmt.setString(3, name);
        pstmt.setString(4, nickname);
        pstmt.setString(5, email);
        pstmt.setString(6, gender);
        pstmt.setString(7, birth);
        pstmt.setString(8, phoneNum);
        pstmt.setString(9, post);
        pstmt.setString(10, fullAddress);

        int result = pstmt.executeUpdate();

        if (result > 0) {
            out.println("<script>alert('회원가입이 완료되었습니다.'); location.href='login.jsp';</script>");
        } else {
            out.println("<script>alert('회원가입에 실패했습니다. 다시 시도해주세요.'); history.back();</script>");
        }

    } catch(Exception e) {
        e.printStackTrace();
        out.println("<script>alert('서버 오류가 발생했습니다.'); history.back();</script>");
    } finally {
        if (pstmt != null) try { pstmt.close(); } catch (Exception e) {}
        if (conn != null) try { conn.close(); } catch (Exception e) {}
    }
%>