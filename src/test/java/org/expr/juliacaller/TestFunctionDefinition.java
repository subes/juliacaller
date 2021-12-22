package org.expr.juliacaller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestFunctionDefinition {

	static JuliaCaller caller;

	@BeforeAll
	public static void init() throws IOException {
		System.out.println("* Initializing tests");
		// caller = new JuliaCaller("/usr/local/bin/julia", 8000);
		// caller = new JuliaCaller("/usr/bin/julia", 8000);
		caller = new JuliaCaller(Utilities.TryFindingJuliaExecutable(), 8001);
		caller.startServer();
		caller.Connect();
	}

	@AfterAll
	public static void finish() throws IOException {
		final Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("* Shuting down the server in 1 second(s)");
				for (int i = 0; i < 1; i++) {
					try {
						Thread.sleep(1000);
						System.out.print(".");
					} catch (final InterruptedException ie) {

					}
				}
			};
		});
		th.start();
		try {
			th.join();
		} catch (final InterruptedException e) {

		}
		caller.ShutdownServer();
	}

	@Test
	public void defineFunctionAndCallTest() throws IOException {
		final String code = "function mysum(x, y)\n" //
				+ "  total = x + y\n" //
				+ "  return total\n" //
				+ "end";
		caller.ExecuteDefineFunction(code);
		caller.Execute("jresult = mysum(3, 5)");

		final int result = caller.getInt("jresult");
		assertEquals(8, result);
	}

	@Test
	public void defineFunctionAndCallTestMultipleDispatch() throws IOException {
		final String code = "function mysum(x::Int64, y::Int64)::Int64\n" //
				+ "  total = x + y\n" //
				+ "  return total\n" //
				+ "end\n" //
				+ "\n" //
				+ "function mysum(x::Float64, y::Float64)::Float64\n" //
				+ "  total = x + y\n" //
				+ "  return total\n" //
				+ "end";

		caller.ExecuteDefineFunction(code);

		caller.Execute("jresult = mysum(3, 5)");
		final int result = caller.getInt("jresult");
		assertEquals(8, result);

		caller.Execute("jresult = mysum(3.1, 5.1)");
		final double resultdbl = caller.getDouble("jresult");
		assertEquals(8.2, resultdbl);

	}
}
