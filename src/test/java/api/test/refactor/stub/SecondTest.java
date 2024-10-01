package api.test.refactor.stub;

import api.common.RestAssuredSetUp;
import api.common.StubServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SecondTest {
    @BeforeAll
    static void setUp(){
        StubServer.startStubServer();
    }

    @Test
    void doSomeThing2(){
        System.out.println("hihi");
    }
}
