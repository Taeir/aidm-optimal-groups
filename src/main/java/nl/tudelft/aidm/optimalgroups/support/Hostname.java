package nl.tudelft.aidm.optimalgroups.support;

import java.util.Objects;

public class Hostname
{
	private String name;

	private Hostname(String name)
	{
		Objects.requireNonNull(name, "Hostname cannot be null");
		this.name = name;
	}

	public String toString()
	{
		return name;
	}

	public static Hostname ofThisMachine()
	{
		return new Hostname(System.getenv("COMPUTERNAME"));
	}

// Alternative stuff for later, if needed:
//String os = System.getProperty("os.name").toLowerCase();
//		if (os.contains("win")) {
//
//
//} else if (os.contains("nix") || os.contains("nux") || os.contains("mac os x")) {
//
//}
//		throw new RuntimeException("Could not determine Hostname: could not determine the OS :/");
//          System.out.println("Windows computer name through exec:\"" + execReadToString("hostname") + "\"");
//			System.out.println("Unix-like computer name through exec:\"" + execReadToString("hostname") + "\"");
//			System.out.println("Unix-like computer name through /etc/hostname:\"" + execReadToString("cat /etc/hostname") + "\"");
//
//		private static String execReadToString(String execCommand) throws IOException
//		{
//			try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
//				return s.hasNext() ? s.next() : "";
//		}

}
