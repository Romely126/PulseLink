<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String id = request.getParameter("id");
    String password = request.getParameter("password");
    String confirmPassword = request.getParameter("confirmPassword");

    if (id == null || id.trim().equals("")) {
        out.println("<script>alert('아이디를 입력하세요.'); history.back();</script>");
        return;
    }
    if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
        out.println("<script>alert('비밀번호가 일치하지 않거나 누락되었습니다.'); history.back();</script>");
        return;
    }
%>
