package com.ilovepc.project_home.web.auth.handler;

import com.ilovepc.project_home.web.auth.vo.signin.SignInRetValCode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignInRetValCodeTypeHandler extends BaseTypeHandler<SignInRetValCode> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SignInRetValCode parameter, JdbcType jdbcType) throws SQLException {
        // MyBatis가 자바 객체를 DB에 저장할 때 사용 (현재는 불필요)
        ps.setString(i, parameter.getCode());
    }

    @Override
    public SignInRetValCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 컬럼 이름을 통해 ResultSet에서 값을 읽을 때
        String code = rs.getString(columnName);
        return SignInRetValCode.fromCode(code); // 1.에서 정의한 변환 메서드 사용
    }

    @Override
    public SignInRetValCode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 컬럼 인덱스를 통해 ResultSet에서 값을 읽을 때
        String code = rs.getString(columnIndex);
        return SignInRetValCode.fromCode(code);
    }

    @Override
    public SignInRetValCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // Stored Procedure의 OUT 파라미터를 읽을 때
        String code = cs.getString(columnIndex);
        return SignInRetValCode.fromCode(code);
    }
}
