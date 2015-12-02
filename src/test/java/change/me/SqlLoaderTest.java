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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

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

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @EndpointInject(uri = "mock:setupComplete")
    protected MockEndpoint setupCompleteEndpoint;

    @EndpointInject(uri = "mock:extractResult")
    protected MockEndpoint extractResultEndpoint;

    @EndpointInject(uri = "mock:S3Result")
    protected MockEndpoint s3ResultEndpoint;

    @EndpointInject(uri = "mock:sqlResult")
    protected MockEndpoint sqlResultEndpoint;


    //a test data set loaded from file is inserted into SQL
    //mock the endpoint
    //post the test data on the route
    //verify the data is in sql
    //one option, avoiding any orm code: http://dbunit.sourceforge.net/howto.html#assertdata
    //another option: use a camel sql query
    @DirtiesContext
    @Test
    public void testFullETL() throws Exception {
        //the table is clear at start
        setupCompleteEndpoint.expectedBodiesReceived("[{count=0}]");
        //we created 1 extract
        extractResultEndpoint.expectedMessageCount(1);
        //we uploaded 1 extract to S3
        s3ResultEndpoint.expectedMessageCount(1);
        //we have 10 records in the warehouse
        sqlResultEndpoint.expectedBodiesReceived("[{count=10}]");

        template.sendBody("");

        setupCompleteEndpoint.assertIsSatisfied();
        extractResultEndpoint.assertIsSatisfied();
        s3ResultEndpoint.assertIsSatisfied();
        sqlResultEndpoint.assertIsSatisfied();
    }
}

