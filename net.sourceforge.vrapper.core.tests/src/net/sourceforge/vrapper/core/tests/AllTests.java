package net.sourceforge.vrapper.core.tests;

import net.sourceforge.vrapper.core.tests.cases.BlockwiseVisualModeTests;
import net.sourceforge.vrapper.core.tests.cases.CommandLineTests;
import net.sourceforge.vrapper.core.tests.cases.InsertModeTests;
import net.sourceforge.vrapper.core.tests.cases.KeyMapTests;
import net.sourceforge.vrapper.core.tests.cases.MacroTests;
import net.sourceforge.vrapper.core.tests.cases.MotionTests;
import net.sourceforge.vrapper.core.tests.cases.NormalModeTests;
import net.sourceforge.vrapper.core.tests.cases.RemappingTests;
import net.sourceforge.vrapper.core.tests.cases.SearchModeTests;
import net.sourceforge.vrapper.core.tests.cases.SimpleKeyStrokeTests;
import net.sourceforge.vrapper.core.tests.cases.SnapshotTests;
import net.sourceforge.vrapper.core.tests.cases.StateAndTransitionTests;
import net.sourceforge.vrapper.core.tests.cases.VisualModeTests;
import net.sourceforge.vrapper.core.tests.cases.VisualModeExclusiveTests;
import net.sourceforge.vrapper.core.tests.cases.VisualModeInclusiveTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CommandLineTests.class,
	InsertModeTests.class,
	KeyMapTests.class,
	MacroTests.class,
	MotionTests.class,
	NormalModeTests.class,
	SearchModeTests.class,
	RemappingTests.class,
	SimpleKeyStrokeTests.class,
	SnapshotTests.class,
	StateAndTransitionTests.class,
	VisualModeTests.class,
	VisualModeInclusiveTests.class,
	VisualModeExclusiveTests.class,
	BlockwiseVisualModeTests.class,
//	VrapperRCTests.class,
//	TextObjectsUnitTests.class,
//	CommandUnitTests.class,
//	CompatibilityTests.class,
}) public class AllTests { }
