package io.stalk.mod.shortener.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class ServerVerticle extends BusModBase implements Handler<HttpServerRequest> {
	
	public static final int DEFAULT_PORT = 8888;
	public static final String DEFAULT_ADDRESS = "0.0.0.0";
	
	@Override
	public void start(final Future<Void> startedResult) {
		super.start(); 
		
		HttpServer server = vertx.createHttpServer();
		server.requestHandler(this);
		
		server.listen(
				getOptionalIntConfig("port", DEFAULT_PORT), 
				getOptionalStringConfig("host", DEFAULT_ADDRESS), 
				new AsyncResultHandler<HttpServer>() {

					@Override
					public void handle(AsyncResult<HttpServer> ar) {
						
						if (!ar.succeeded()) {
							startedResult.setFailure(ar.cause());
						} else {
							startedResult.setResult(null);
						}
						
						logger.info(" >>> server is started");
						
					}

				});
	}
	
	@Override
	public void handle(final HttpServerRequest req) {
		
		if(req.path().startsWith("/url/")){

			String url = req.uri().substring(5);
			try {
				url = URLDecoder.decode(url, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			JsonObject data = new JsonObject();
			data.putString("action", "create");
			data.putString("url", url);
			
			eb.send("shortener.persister", data, 
					new Handler<Message<JsonObject>>(){

				@Override
				public void handle(Message<JsonObject> message) {

					String callback_jsonp = req.params().get("callback");
					
					req.response().putHeader("content-type", "application.json; charset=UTF-8");
					
					if(callback_jsonp != null){
						req.response().end(callback_jsonp+"("+message.body().encode()+")");	
					}else{
						req.response().end(message.body().encode());
					}
					  
				}

			});

		}else{
			
			String key = req.path().substring(1);

			JsonObject data = new JsonObject();
			data.putString("action", "get");
			data.putString("key", key);

			eb.send("shortener.persister", data, 
					new Handler<Message<JsonObject>>(){

				@Override
				public void handle(Message<JsonObject> message) {

					if("error".equals(message.body().getString("status"))){

						req.response().setStatusCode(404);
						req.response().end();

					}else{
						
						req.response().setStatusCode(301);
						req.response().putHeader("Location", message.body().getString("url"));
						req.response().end();
					}

				}

			});
		}
		
	}

}
