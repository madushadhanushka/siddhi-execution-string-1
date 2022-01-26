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

public class RepeatFunctionExtensionTestCase {
    private static final Logger LOGGER = LogManager.getLogger(RepeatFunctionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testRepeatFunctionExtension() throws InterruptedException {
        LOGGER.info("RepeatFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, price long, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , " +
                        "str:repeat(symbol, 3) as symbolRepeated3Times " +
                        "insert into outputStream;"
        );

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals("StRing 1StRing 1StRing 1", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals("StrInG 2StrInG 2StrInG 2", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals("Str 3Str 3Str 3", event.getData(1));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"StRing 1", 700f, 100L});
        inputHandler.send(new Object[]{"StrInG 2", 60.5f, 200L});
        inputHandler.send(new Object[]{"Str 3", 60.5f, 200L});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testRepeatFunctionExtensionVariableIndex() throws InterruptedException {
        LOGGER.info("RepeatFunctionExtension - VariableIndex - TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, times int, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , " +
                        "str:repeat(symbol, times) as symbolRepeatedIndexTimes " +
                        "insert into outputStream;"
        );

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals("StRing 1StRing 1StRing 1", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals("StrInG 2StrInG 2", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals("Str 3Str 3Str 3Str 3Str 3Str 3", event.getData(1));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"StRing 1", 3, 100L});
        inputHandler.send(new Object[]{"StrInG 2", 2, 200L});
        inputHandler.send(new Object[]{"Str 3", 6, 200L});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testRepeatFunctionExtension1() throws InterruptedException {
        LOGGER.info("RepeatFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, times int, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:repeat(volume, times) as "
                        + "symbolRepeatedIndexTimes " +
                        "insert into outputStream;"
        );
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testRepeatFunctionExtension2() throws InterruptedException {
        LOGGER.info("RepeatFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, times int, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:repeat(symbol, volume) as "
                        + "symbolRepeatedIndexTimes " +
                        "insert into outputStream;"
        );

        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testRepeatFunctionExtension3() throws InterruptedException {
        LOGGER.info("RepeatFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, times int, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:repeat(symbol) as "
                        + "symbolRepeatedIndexTimes " +
                        "insert into outputStream;"
        );
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test
    public void testRepeatFunctionExtension4() throws InterruptedException {
        LOGGER.info("RepeatFunctionExtension TestCase with null value");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, times int, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:repeat(symbol, times) as "
                        + "symbolRepeatedIndexTimes " +
                        "insert into outputStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime =  siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{null, 3, 100L});
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void testRepeatFunctionExtension5() throws InterruptedException {
        LOGGER.info("RepeatFunctionExtension TestCase with null value");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, times int, volume long);";

        String query = (
                "@info(name = 'query1') from inputStream select symbol , str:repeat(symbol, times) as "
                        + "symbolRepeatedIndexTimes " +
                        "insert into outputStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"WSO2", null, 100L});
        siddhiAppRuntime.shutdown();
    }
}
