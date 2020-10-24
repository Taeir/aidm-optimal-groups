package nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver.minizinc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MiniZinc
{
	private final static String binary = "minizinc.exe";

	// TODO: Configurable
	private final static String workingDir = "C:/Program Files/MiniZinc/";

	private final URL model;
	public MiniZinc(URL model)
	{
		this.model = model;
	}

	private Process spawnMinizincProcess(JsonDatafile modelData, String solver, long timelimit) throws Exception
	{
		var dataFile = File.createTempFile("minizinc_data_" + Instant.now().getEpochSecond(), ".json");
		var writer = new FileWriter(dataFile);
		writer.write(modelData.asJsonString());
		writer.close();

		var proc = Runtime.getRuntime().exec(new String[] {
			workingDir + binary,
			"--solver", solver,
			"--time-limit", String.valueOf(timelimit),
			"-O3",
			new File(model.getFile()).getAbsolutePath(),
			dataFile.getAbsolutePath()
		}, null, new File(workingDir));

		return proc;
	}

	public Solutions run(JsonDatafile data, String solver, long timelimit) throws Exception
	{
		var proc = spawnMinizincProcess(data, solver, timelimit);

		InputStream inputStream = proc.getInputStream();
		proc.waitFor(5, TimeUnit.MINUTES);

		return new Solutions(inputStream);
	}

	public static class Solutions
	{
		private List<String> rawSolutions;

		// Note: not very nice to pass in InputStream assuming it's closed and done.
		// Future work: read from an open stream on the fly, blocking methods, the whole thing
		Solutions(InputStream inputStream) throws Exception
		{
			var minizincStdOutputAsString = new String(inputStream.readAllBytes());

			rawSolutions = new ArrayList<>();

			var solutions = minizincStdOutputAsString.split("----------[\n\r]+");

			rawSolutions = Arrays.stream(solutions)
				.filter(s -> !s.startsWith("=========="))
				.collect(Collectors.toList());
		}

		public List<String> asList()
		{
			return Collections.unmodifiableList(rawSolutions);
		}

		public String bestRaw()
		{
			int last = rawSolutions.size() - 1;
			return rawSolutions.get(last);
		}
	}
}
