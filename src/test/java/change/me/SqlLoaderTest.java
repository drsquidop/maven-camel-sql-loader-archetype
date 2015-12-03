package change.me;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
//import org.apache.camel.spring.SpringRunWithTestSupport;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.junit4.TestSupport;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.DisableJmx;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

/**
 * User: doug
 * Date: 12/1/15
 * Time: 9:46 PM
 * The Sql Loader should receive message(s) on the bus and either insert or replace it in the sql table
 */
@TestPropertySource("file:etc/server.properties")
@RunWith(CamelSpringJUnit4ClassRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration({ "classpath:SqlLoaderTest-context.xml" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@MockEndpoints("log:*")
@DisableJmx(false)
public class SqlLoaderTest {

    @Produce(uri = "ref:replace")
    protected ProducerTemplate template;

    @Produce(uri = "direct:replaceCsv")
    protected ProducerTemplate insertCsvEndpoint;

    @EndpointInject(uri = "mock:sqlRowsReplaced")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:clear")
    protected ProducerTemplate clearEndpoint;

    @EndpointInject(uri = "mock:cleared")
    protected MockEndpoint clearedEndpoint;

    @DirtiesContext
    @Before
    public void beforeEach() throws Exception {
        //delete all rows
        clearedEndpoint.expectedMessageCount(1);
        clearEndpoint.sendBody("");
        clearedEndpoint.assertIsSatisfied();
    }

    //a test data set loaded from file is inserted into SQL
    //mock the endpoint
    //post the test data on the route
    //verify the data is in sql
    //one option, avoiding any orm code: http://dbunit.sourceforge.net/howto.html#assertdata
    //another option: use a camel sql query
    @DirtiesContext
    @Test
    public void testSendEmptyList() throws Exception {
        //it should not send 'replaced' if the body is an empty list
        resultEndpoint.expectedMessageCount(0);

        //create our test data
        List testData = new ArrayList();

        template.sendBody(testData);

        resultEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void testInsert1Row() throws Exception {
        //we have 10 records in the warehouse
        resultEndpoint.expectedBodiesReceived("[{COUNT=1}]");
        resultEndpoint.expectedMessageCount(1);

        //create our test data
        String testData = "1,Joe";

        insertCsvEndpoint.sendBody(testData);

        resultEndpoint.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void testInsertNRows() throws Exception {
        //resultEndpoint.expectedBodiesReceived("[{COUNT=2}]");
        resultEndpoint.expectedMessageCount(2);
        resultEndpoint.message(1).body().isEqualTo("[{COUNT=2}]");

        //create our test data
        String testData = "1,Joe\n2,Steve";

        insertCsvEndpoint.sendBody(testData);

        resultEndpoint.assertIsSatisfied();
    }
}

