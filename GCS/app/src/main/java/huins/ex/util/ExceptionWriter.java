package huins.ex.util;

import android.content.Context;

import java.io.PrintStream;

public class ExceptionWriter {
	private Throwable exception;

	public ExceptionWriter(Throwable ex) {
		this.exception = ex;
	}

	public void saveStackTraceToSD(Context context) {
		try {
			PrintStream out = new PrintStream(FileStream.getExceptionFileStream(context));
			exception.printStackTrace(out);
			out.close();
		} catch (Exception excep) {
			excep.printStackTrace();
		}
	}
}
