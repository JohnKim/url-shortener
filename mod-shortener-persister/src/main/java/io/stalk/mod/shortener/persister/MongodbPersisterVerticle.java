package io.stalk.mod.shortener.persister;

import java.net.UnknownHostException;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongodbPersisterVerticle extends Verticle {

	private JsonObject config;
	private String address;
	private String host;
	private int port;
	private MongoClient mongo;
	private DB db;
	private Logger logger;
	private EventBus eb;

	@Override
	public void start() {

		super.start();


		config = container.config();
		logger = container.logger();
		eb = vertx.eventBus();


		logger.info(" >>> mongodbPersisterVerticle is starting .... ");

		address = config.getString("address") == null ? "shortener.persister" : config.getString("address");
		host = config.getString("host") == null ? "localhost" : config.getString("host");
		port = config.getNumber("port") == null ? 27017 : config.getNumber("port").intValue();

		try {
			ServerAddress mongoAddress = new ServerAddress(host, port);
			mongo = new MongoClient(mongoAddress);
			db = mongo.getDB("url-shortener");

		} catch (UnknownHostException e) {
			logger.error(" >> mongodb connection error : ", e);
			e.printStackTrace();
		}


		logger.info(" >>> mongodbPersisterVerticle is registering event - "+address);

		eb.registerHandler(address, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> message) {
				String action = message.body().getString("action");
				switch (action) {
				case "create":
					createShortUrl(message);
					break;

				case "get":
					getShortUrl(message);
					break;

				default:
					JsonObject errMsg = new JsonObject().putString("status", "error").putString("message", "[action] must be specified!");
					message.reply(errMsg);
				}

			}
		});
		
		logger.info(" >>> mongodbPersister server is started.");

	}

	@Override
	public void stop() {
		if (mongo != null) {
			mongo.close();
		}
	}

	protected void getShortUrl(Message<JsonObject> message) {

		DBCollection urls = db.getCollection("urls");

		DBObject query = new BasicDBObject("_id", message.body().getString("key"));

		DBObject res = urls.findOne(query);

		if(res != null){
			message.reply(new JsonObject(res.toString()).putString("status", "ok"));
		}else{
			message.reply(new JsonObject().putString("status", "error").putString("message", "NOT EXISTED."));
		}
		

	}

	protected void createShortUrl(Message<JsonObject> message) {

		DBCollection seq = db.getCollection("seq");

		// 1. generate shortId (key) ;
		DBObject query = new BasicDBObject();
		query.put("_id", "urlShortener");

		logger.info(message.body().getString("url"));
		
		DBObject change = new BasicDBObject("seq", 1);
		DBObject update = new BasicDBObject("$inc", change);

		DBObject res = seq.findAndModify(
				query,  new BasicDBObject(), new BasicDBObject(), false, update, true, true);

		int num = Integer.parseInt(res.get("seq").toString());
		String shortId = BijectiveUtil.encode(num);

		// 2. store new URL shortened.
		DBCollection urls = db.getCollection("urls");

		DBObject urlData = new BasicDBObject("_id", shortId);
		urlData.put("url", message.body().getString("url"));

		urls.insert(urlData);

		JsonObject reply = new JsonObject();
		reply.putString("key", shortId);
		reply.putString("url", message.body().getString("url"));

		reply.putString("status", "ok");

		message.reply(reply);
	}

}
