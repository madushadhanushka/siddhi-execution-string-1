/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.extension.execution.string;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.extension.execution.string.test.util.SiddhiTestHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class HexFunctionExtensionTestCase {
    protected static SiddhiManager siddhiManager;
    private static final Logger LOGGER = LogManager.getLogger(HexFunctionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);

    @BeforeMethod
    public void init() {
        count.set(0);
    }

    @Test
    public void testProcess() throws Exception {
        LOGGER.info("HexFunctionExtension TestCase");

        siddhiManager = new SiddhiManager();
        String inValueStream = "define stream InValueStream (inValue string);";

        String eventFuseSiddhiApp = ("@info(name = 'query1') from InValueStream "
                + "select str:hex(inValue) as hexString "
                + "insert into OutMediationStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inValueStream + eventFuseSiddhiApp);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents,
                                Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                String result;
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    result = (String) event.getData(0);
                    AssertJUnit.assertEquals("4d7953514c", result);
                }
            }
        });
        InputHandler inputHandler = siddhiAppRuntime
                .getInputHandler("InValueStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"MySQL"});
        SiddhiTestHelper.waitForEvents(1000, 1, count, 60000);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testWithZeroArguments() throws Exception {
        LOGGER.info("HexFunctionExtension TestCase with zero arguments");

        siddhiManager = new SiddhiManager();
        String inValueStream = "define stream InValueStream (inValue string);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from InValueStream "
                + "select str:hex() as hexString "
                + "insert into OutMediationStream;");
        siddhiManager.createSiddhiAppRuntime(inValueStream + eventFuseExecutionPlan);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testWithInvalidDatatype() throws Exception {
        LOGGER.info("HexFunctionExtension TestCase with invalid data type");

        siddhiManager = new SiddhiManager();
        String inValueStream = "define stream InValueStream (inValue int);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from InValueStream "
                + "select str:hex(inValue) as hexString "
                + "insert into OutMediationStream;");
        siddhiManager.createSiddhiAppRuntime(inValueStream + eventFuseExecutionPlan);
    }

    @Test
    public void testWithNullValue() throws Exception {
        LOGGER.info("HexFunctionExtension TestCase with null value");

        siddhiManager = new SiddhiManager();
        String inValueStream = "define stream InValueStream (inValue string);";

        String eventFuseExecutionPlan = ("@info(name = 'query1') from InValueStream "
                + "select str:hex(inValue) as hexString "
                + "insert into OutMediationStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime
                (inValueStream + eventFuseExecutionPlan);

        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime
                .getInputHandler("InValueStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{null});
        siddhiAppRuntime.shutdown();
    }
}
