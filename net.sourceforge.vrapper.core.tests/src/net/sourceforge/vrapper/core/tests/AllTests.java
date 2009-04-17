package net.sourceforge.vrapper.core.tests;

import net.sourceforge.vrapper.core.tests.cases.SimpleKeyStrokeTests;
import net.sourceforge.vrapper.core.tests.cases.StateAndTransitionTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	StateAndTransitionTests.class,
	SimpleKeyStrokeTests.class,
//	MotionTests.class,
//	NormalModeTests.class,
//	InsertModeTests.class,
//	VisualModeTests.class,
//	TextObjectsUnitTests.class,
//	CommandUnitTests.class,
//	CompatibilityTests.class,
}) public class AllTests { }
