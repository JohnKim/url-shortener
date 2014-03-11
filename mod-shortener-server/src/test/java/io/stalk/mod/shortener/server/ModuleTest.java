package io.stalk.mod.shortener.server;

import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.testtools.TestVerticle;

public class ModuleTest extends TestVerticle {

	@Test
	public void testSomethingElse() {
		// Whatever
		testComplete();
	}

	@Override
	public void start() {

		initialize();
		container.deployModule(System.getProperty("vertx.modulename"), new AsyncResultHandler<String>() {
			@Override
			public void handle(AsyncResult<String> asyncResult) {

				if(!asyncResult.succeeded()){
					asyncResult.cause().printStackTrace();
				}
				assertTrue(asyncResult.succeeded());
				assertNotNull("deploymentID should not be null", asyncResult.result());
				startTests();
			}
		});
	}

}
