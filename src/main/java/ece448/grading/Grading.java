package ece448.grading;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Grading {
	static void run(Object obj, int n) {
		ExecutorService exe = Executors.newSingleThreadExecutor();
		String className = obj.getClass().getSimpleName();		
		int grade = 0;
		try
		{
			for (int i = 0; i < n; i++)
			{
				String testCaseName = String.format("testCase%02d", i);
				try
				{
					Method testCase = obj.getClass().getDeclaredMethod(testCaseName);

					Future<Boolean> f = exe.submit(() -> {
						return (Boolean)testCase.invoke(obj);
					});

					if (f.get(60, TimeUnit.SECONDS))
					{
						System.out.println("*************************************************************");
						System.out.printf("******** %s-%s: success%n", className, testCaseName);
						System.out.println("*************************************************************");
						logger.info("{}-{}: success", className, testCaseName);
						++grade;
					}
					else
					{
						System.out.println("*************************************************************");
						System.out.printf("******** %s-%s: failed%n", className, testCaseName);
						System.out.println("*************************************************************");
						logger.info("{}-{}: failed", className, testCaseName);
					}
				}
				catch (ExecutionException e)
				{
					System.out.println("*************************************************************");
					System.out.printf("******** %s-%s: exception %s%n", className, testCaseName, e.getCause().getCause().toString());
					System.out.println("*************************************************************");
					logger.info("{}-{}: exception {}", className, testCaseName, e.getCause().getCause().toString());
					logger.debug("{}-{}: exception", className, testCaseName, e);
				}
				catch (TimeoutException e)
				{
					logger.info("{}-{}: timeout, abort", className, testCaseName);
					throw new RuntimeException(e);
				}
				catch (Throwable t)
				{
					logger.info("{}-{}: unknown error, abort", className, testCaseName, t);
					throw new RuntimeException(t);
				}
			}
		}
		finally
		{
			System.out.printf("Local Testing: %d cases passed%n", grade);
			System.out.println("*************************************************************");
			System.out.println("* You may receive 0 points unless your code tests correctly *");
			System.out.println("* in CI System. Please commit and push your code to start.  *");
			System.out.println("*************************************************************");
			exe.shutdownNow();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(Grading.class);
}
