package io.ryan.picspace.model.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;


class UserRoleEnumTest {

    @Test
    public void test() {
        UserRoleEnum user = UserRoleEnum.getEnumByValue("user");
        System.out.println(user.getText());
    }

}