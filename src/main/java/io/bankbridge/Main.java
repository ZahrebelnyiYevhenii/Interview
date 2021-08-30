package io.bankbridge;
import static spark.Spark.get;
import static spark.Spark.port;

import io.bankbridge.handler.BanksCacheBased;
import io.bankbridge.handler.BanksRemoteCalls;

public class Main {

	public static void main(String[] args) throws Exception {
		
		port(8070);

		BanksCacheBased banksCacheBased = new BanksCacheBased();
		BanksRemoteCalls banksRemoteCalls = new BanksRemoteCalls();

		banksCacheBased.init();
		banksRemoteCalls.init();

		get("/v1/banks/all", banksCacheBased::handle);
		get("/v2/banks/all", banksRemoteCalls::handle);
		get("/v1/banks", banksCacheBased::filter);
		get("/v2/banks", banksRemoteCalls::filter);
	}
}