package tubs.cs.studienrichtung.util;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class MessageConsoleOutput implements Output {

	private MessageConsoleStream out, warn, error;
	
	public MessageConsoleOutput(MessageConsole console) {		
		out = console.newMessageStream();
		warn = console.newMessageStream();
		error = console.newMessageStream();
		
		//out.setColor(new Color(d, new RGB(0, 0, 0)));
		//warn.setColor(new Color(d, new RGB(1, 1, 0)));
		//error.setColor(new Color(d, new RGB(1, 0, 0)));
	}
	
	@Override
	public void print(Object o) {
		out.print(o.toString());
	}

	@Override
	public void println(Object o) {
		out.println(o.toString());
	}

	@Override
	public void err(String s) {
		error.println("[ERROR] " + s);
	}

	@Override
	public void warn(String s) {
		warn.println("[WARNING] " + s);
	}
}
