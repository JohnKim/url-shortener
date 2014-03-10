package io.stalk.mod.shortener.persister;

/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModuleTest extends TestVerticle {

	static String shortId;

	@Test
	public void test_1_createShortUrl(){

		container.logger().info("[MongodbPersisterVerticle] createShortUrl");

		JsonObject data = new JsonObject();
		data.putString("action", "create");
		data.putString("url", "https://www.facebook.com/groups/serverside/");

		vertx.eventBus().send("shortener.persister", data, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> reply) {
				
				shortId = reply.body().getString("key");
				System.out.println(reply.body());
				assertEquals("the status of message replied must be 'ok'. ", "ok", reply.body().getString("status"));

				testComplete();
			}
		}); 
	}

	@Test
	public void test_2_getShortUrl(){

		container.logger().info("[MongodbPersisterVerticle] getShortUrl");

		JsonObject data = new JsonObject();
		data.putString("action", "get");
		data.putString("key", shortId);

		vertx.eventBus().send("shortener.persister", data, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> reply) {
				System.out.println(reply.body());
				assertEquals("the status of message replied must be 'ok'. ", "ok", reply.body().getString("status"));

				testComplete();
			}

		}); 

	}


	@Override
	public void start() {
		
		initialize();

		container.logger().info("[MongodbPersisterVerticle] module name : "+System.getProperty("vertx.modulename"));

		container.deployModule(System.getProperty("vertx.modulename") ,
				new AsyncResultHandler<String>() {
			@Override
			public void handle(AsyncResult<String> asyncResult) {
				assertTrue(asyncResult.succeeded());
				assertNotNull("deploymentID should not be null",
						asyncResult.result());

				startTests();
			}
		}); 

		startTests();
	}

}
