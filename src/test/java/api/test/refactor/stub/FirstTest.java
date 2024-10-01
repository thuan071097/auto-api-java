package api.test.refactor.stub;

import api.common.RestAssuredSetUp;
import api.common.StubServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FirstTest {
    @BeforeAll
    static void setUp(){
        StubServer.startStubServer();
    }

    @Test
    void doSomeThing1(){
        System.out.println("hahaha");
    }
}
