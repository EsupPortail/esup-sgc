package org.esupportail.sgc.tools;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * Pour récupérer l'exception dans une tâche en @async 
 * Voir
 * https://jira.spring.io/browse/SPR-8995 
 * et solution de contournement donnée sur
 * http://java.dzone.com/articles/spring-async-and-exception
 * et employée ici-même ... en attendant spring 4.1 :)
 * 
 */
public class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private String logLevel = "ERROR";

	private AsyncTaskExecutor executor;

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public ExceptionHandlingAsyncTaskExecutor(AsyncTaskExecutor executor) {
		this.executor = executor;
	}

	public void execute(Runnable task) {
		executor.execute(createWrappedRunnable(task));
	}

	public void execute(Runnable task, long startTimeout) {
		executor.execute(createWrappedRunnable(task), startTimeout);
	}

	public Future submit(Runnable task) {
		return executor.submit(createWrappedRunnable(task));
	}

	@Override
	public <T> Future<T> submit(final Callable<T> task) {
		return executor.submit(createCallable(task));
	}

	private <T> Callable<T> createCallable(final Callable<T> task) {
		return new Callable<T>() {
			public T call() throws Exception {
				try {
					return task.call();
				} catch (Exception ex) {
					handle(ex);
					throw ex;
				}
			}
		};
	}

	private Runnable createWrappedRunnable(final Runnable task) {
		return new Runnable() {
			public void run() {
				try {
					task.run();
				} catch (Exception ex) {
					handle(ex);
				}
			}
		};
	}

	private void handle(Exception ex) {
		// warn pour éviter pollution par mails ... car trop d'erreurs avec easyid
		if("WARN".equals(logLevel)) {
			log.warn("Error during @Async execution.", ex);
		} else {
			log.error("Error during @Async execution.", ex);
		}
	}
}
