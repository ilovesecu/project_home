package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.auth.vo.signup.SignUpParam;
import org.apache.ibatis.annotations.Select;

@HomeMaster
public interface AuthMasterMapper {
    @Select("CALL home_project.p_sign_up(#{email}, #{password},#{nickname})")
    int pSignUp(SignUpParam param);
}
