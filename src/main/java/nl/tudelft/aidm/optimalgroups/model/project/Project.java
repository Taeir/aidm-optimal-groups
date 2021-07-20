package nl.tudelft.aidm.optimalgroups.model.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Project
{
	String name();
	int id();
	List<ProjectSlot> slots();

	class ProjectsWithDefaultSlotAmount extends ProjectWithStaticSlotAmount
	{
		private final static int DEFAULT_NUM_SLOTS_VALUE = 5;

		public ProjectsWithDefaultSlotAmount(int id)
		{
			super(id, DEFAULT_NUM_SLOTS_VALUE);
		}
	}

	/**
	 * Project with a default / hardcoded amount of slots
	 */
	class ProjectWithStaticSlotAmount implements Project
	{
		private int id;
		private List<ProjectSlot> slots;

		public ProjectWithStaticSlotAmount(int id, int numSlots)
		{
			this.id = id;

			slots = new ArrayList<>(numSlots);
			for (int i = 0; i < numSlots; i++)
			{
				var slot = new ProjectSlot.Simple(i, this);

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

		@Override
		public String toString()
		{
			return name();
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (!(o instanceof Project))
			{
				return false;
			}
			Project that = (Project) o;
			return id == that.id();
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(id);
		}
	}

	interface ProjectSlot
	{
		String id();
		int index();
		Project belongingToProject();

		class Simple implements ProjectSlot {

			private final String id;
			private final int index;
			private final Project projectBelongsTo;

			public Simple(int index, Project projectBelongsTo)
			{
				this.index = index;
				this.projectBelongsTo = projectBelongsTo;

				this.id = projectBelongsTo + "_slot_" + index;
			}

			public String id()
			{
				return id;
			}

			public int index()
			{
				return index;
			}

			public Project belongingToProject()
			{
				return projectBelongsTo;
			}

			@Override
			public String toString()
			{
				return id();
			}
		}
	}
}
