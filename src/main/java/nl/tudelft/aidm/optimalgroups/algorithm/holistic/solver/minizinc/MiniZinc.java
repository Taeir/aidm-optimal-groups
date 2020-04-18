package nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver.minizinc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MiniZinc
{
	private final static String binary = "minizinc";

	// TODO: Configurable
	private final static String workingDir = "C:\\Program Files\\MiniZinc IDE (bundled)";

	public String exec()
	{
		return "";
	}

	private Process spawnMinizincProcess(String... args) throws IOException
	{
		List<String> command = new ArrayList<>(args.length + 1);
		command.add(binary);
		command.addAll(List.of(args));

		ProcessBuilder pb = new ProcessBuilder(command);

		return pb.start();
	}
}
