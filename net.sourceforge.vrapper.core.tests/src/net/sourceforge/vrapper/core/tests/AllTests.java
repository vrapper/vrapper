package net.sourceforge.vrapper.core.tests;

import net.sourceforge.vrapper.core.tests.cases.KeyMapTests;
import net.sourceforge.vrapper.core.tests.cases.MotionTests;
import net.sourceforge.vrapper.core.tests.cases.NormalModeTests;
import net.sourceforge.vrapper.core.tests.cases.SimpleKeyStrokeTests;
import net.sourceforge.vrapper.core.tests.cases.SnapshotTests;
import net.sourceforge.vrapper.core.tests.cases.StateAndTransitionTests;
import net.sourceforge.vrapper.core.tests.cases.VisualModeTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	StateAndTransitionTests.class,
	SimpleKeyStrokeTests.class,
	MotionTests.class,
	NormalModeTests.class,
	KeyMapTests.class,
	VisualModeTests.class,
	SnapshotTests.class,
//	InsertModeTests.class,
//	TextObjectsUnitTests.class,
//	CommandUnitTests.class,
//	CompatibilityTests.class,
}) public class AllTests { }
