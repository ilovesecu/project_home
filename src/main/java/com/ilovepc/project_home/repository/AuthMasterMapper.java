package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.config.security.vo.User;
import com.ilovepc.project_home.web.auth.handler.SignInRetValCodeTypeHandler;
import com.ilovepc.project_home.web.auth.vo.signin.SignInParam;
import com.ilovepc.project_home.web.auth.vo.signin.SignInResult;
import com.ilovepc.project_home.web.auth.vo.signup.SignUpParam;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@HomeMaster
public interface AuthMasterMapper {
    @Select("CALL home_project.p_sign_up(#{email}, #{password},#{nickname})")
    int pSignUp(SignUpParam param);

    @Select("CALL home_project.p_sign_in(#{email},#{memIp})")
    @Results({
            @Result(column = "retVal", property = "resultCode", typeHandler = SignInRetValCodeTypeHandler.class),
            @Result(column = "memNo", property = "userNo")
    })
    SignInResult pSignIn(SignInParam param);

    @Select("CALL home_project.p_user_sel(#{userNo})")
    User pUserSel(String userNo);
}
