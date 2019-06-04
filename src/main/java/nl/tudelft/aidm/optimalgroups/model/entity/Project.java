package nl.tudelft.aidm.optimalgroups.model.entity;

import java.util.ArrayList;
import java.util.List;

public interface Project
{
	String name();
	int id();
	List<ProjectSlot> slots();

	interface ProjectSlot
	{
		String id();
		Project belongingToProject();

		class Simple implements ProjectSlot {

			private String id;
			private Project projectBelongsTo;

			public Simple(String id, Project projectBelongsTo)
			{
				this.id = id;
				this.projectBelongsTo = projectBelongsTo;
			}

			public String id()
			{
				return id;
			}

			public Project belongingToProject()
			{
				return projectBelongsTo;
			}
		}
	}

	/**
	 * Project with a default / hardcoded amount of slots
	 */
	class withDefaultSlots implements Project
	{
		private final static int numSlots = 5;
		private int id;
		private List<ProjectSlot> slots;

		public withDefaultSlots(int id)
		{
			this.id = id;

			slots = new ArrayList<>(numSlots);
			for (int i = 0; i < numSlots; i++)
			{
				var slotId = String.format("proj-%d_slot-%d", id, i);
				var slot = new ProjectSlot.Simple(slotId, this);

				slots.add(slot);
			}
		}

		@Override
		public String name()
		{
			return "proj_" + String.valueOf(id);
		}

		@Override
		public int id() {
			return id;
		}

		@Override
		public List<ProjectSlot> slots()
		{
			return slots;
		}
	}
}
