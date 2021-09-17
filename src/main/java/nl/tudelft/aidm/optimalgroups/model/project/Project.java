package nl.tudelft.aidm.optimalgroups.model.project;

import plouchtch.lang.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Project
{
	String name();
	int sequenceNum();
	
	List<ProjectSlot> slots();
	
	class ProjectWithStaticSlotAmount implements Project
	{
		private final int sequenceNum;
		private List<ProjectSlot> slots;

		public ProjectWithStaticSlotAmount(int sequenceNum, int numSlots)
		{
			this.sequenceNum = sequenceNum;

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
			return "proj_" + sequenceNum;
		}

		@Override
		public int sequenceNum()
		{
			return sequenceNum;
		}

		@Override
		public List<ProjectSlot> slots()
		{
			return slots;
		}

		@Override
		public String toString()
		{
			return "proj_" + sequenceNum;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			
			if ((o instanceof ProjectWithStaticSlotAmount)) {
				Project that = (Project) o;
				return sequenceNum == that.sequenceNum();
			}
			
			return false;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(sequenceNum);
		}
	}

	/**
	 * Project with a bepsys identifier
	 */
	class BepSysProject extends ProjectWithStaticSlotAmount
	{
		public final int bepsysId;
		
		public BepSysProject(int sequenceNum, int bepSysId, int numSlots)
		{
			super(sequenceNum, numSlots);
			this.bepsysId = bepSysId;
		}

		@Override
		public String name()
		{
			return String.format("%s", bepsysId);
		}

		@Override
		public String toString()
		{
			return String.format("proj_%s[bepsys_%s]", sequenceNum(), bepsysId);
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			
			if ((o instanceof BepSysProject)) {
				var that = (BepSysProject) o;
				return bepsysId == that.bepsysId;
			}
			
			return false;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(bepsysId);
		}
	}

	interface ProjectSlot
	{
		String id();
		int index();
		
		Project belongingToProject();

		class Simple implements ProjectSlot {

			private final Lazy<String> id;
			private final int index;
			private final Project projectBelongsTo;

			public Simple(int index, Project projectBelongsTo)
			{
				this.index = index;
				this.projectBelongsTo = projectBelongsTo;

				// Temporal coupling between this class and ProjectFromDB that extends ProjectWithStaticSlotAmount
				// where the bepsys_id is only set after its super and this constructor is done, so workaround by deferring
				this.id = new Lazy<>(() -> projectBelongsTo + "_slot_" + index);
			}

			public String id()
			{
				return id.get();
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
