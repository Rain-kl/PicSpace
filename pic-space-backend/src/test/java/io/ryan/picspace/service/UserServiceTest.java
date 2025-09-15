package io.ryan.picspace.service;

import io.ryan.picspace.PicSpaceBackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PicSpaceBackendApplication.class)
class UserServiceTest {

    @Autowired
    UserService userService;

    @Test
    void test() {
        System.out.println(userService.count());
    }

}