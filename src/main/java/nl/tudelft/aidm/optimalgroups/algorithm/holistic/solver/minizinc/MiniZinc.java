package nl.tudelft.aidm.optimalgroups.algorithm.holistic.solver.minizinc;

import nl.tudelft.aidm.optimalgroups.dataset.bepsys.CourseEdition;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualAgents;
import nl.tudelft.aidm.optimalgroups.model.dataset.sequentual.SequentualDataset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MiniZinc
{
	private final static String binary = "minizinc.exe";

	// TODO: Configurable
	private final static String workingDir = "C:/Program Files/MiniZinc/";
	private final static URL model = MiniZinc.class.getClassLoader().getResource("Grouped_Project_Student_Allocation.mzn");

	public String exec()
	{
		return "";
	}

	private Process spawnMinizincProcess(JsonDatafile modelData, String... args) throws IOException
	{
		var dataFile = File.createTempFile("minizinc_data_" + Instant.now().getEpochSecond(), ".json");
		var writer = new FileWriter(dataFile);
		writer.write(modelData.asJsonString());
		writer.close();

		List<String> command = new ArrayList<>(args.length + 1);

		var proc = Runtime.getRuntime().exec(new String[] { workingDir + binary, "--solver", "COIN-BC", "--time-limit", "300000", "-O3", new File(model.getFile()).getAbsolutePath(), dataFile.getAbsolutePath()});
		return proc;

//		command.add(workingDir + binary);
//		command.add("--solver COIN-BC");
//		command.add("--time-limit 300000");
//		command.add("-O 3");
//		command.add(model);
//		command.add(dataFile.getAbsolutePath());
//
//		command.addAll(List.of(args));
//
//		ProcessBuilder pb = new ProcessBuilder(command);
////		pb.directory(Paths.get(workingDir).toFile());
//
//		return pb.start();
	}

	public static void main(String[] args) throws Exception
	{
		CourseEdition ce = CourseEdition.fromLocalBepSysDbSnapshot(4);

		var seqDataset = SequentualDataset.from(ce);
		var data = new StudentGroupProjectMatchingInstanceData(seqDataset, 5);

		var proc = new MiniZinc().spawnMinizincProcess(data);

		InputStream inputStream = proc.getInputStream();

		proc.waitFor(5, TimeUnit.MINUTES);
		var henk = new String(inputStream.readAllBytes());
	}
}
