package huins.ex.util.File.IO;

import android.content.Context;

import java.io.PrintStream;

import huins.ex.util.File.FileUtils;

public class ExceptionWriter {
	private final Context context;

	public ExceptionWriter(Context context) {
		this.context = context;
	}

	public void saveStackTraceToSD(Throwable exception) {
        if(exception == null)
            return;

		try {
			PrintStream out = new PrintStream(FileUtils.getExceptionFileStream(context));
			exception.printStackTrace(out);
			out.close();
		} catch (Exception excep) {
			excep.printStackTrace();
		}
	}
}
