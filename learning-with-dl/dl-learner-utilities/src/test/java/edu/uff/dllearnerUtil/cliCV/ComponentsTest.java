package edu.uff.dllearnerUtil.cliCV;

import java.util.Collection;

import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.junit.Assert;
import org.junit.Test;

public class ComponentsTest {

	@Test
	public void test() {
		String componentsCheck[] = {"edu.uff.dllearnerUtil.cliCV.RulesLPStandard"};
		CliCV.loadNewComponents();
		AnnComponentManager annComponentManager = AnnComponentManager.getInstance();
		
		for (int i = 0; i < componentsCheck.length; i++) {
			String componentName = componentsCheck[i];
			try {
				Collection<?> components = annComponentManager.getComponentsOfType(Class.forName(componentName));
				Assert.assertNotNull(components);
				Assert.assertFalse(components.isEmpty());
			} catch (ClassNotFoundException e) {
				Assert.fail(e.toString());
			}
		}
		
	}

}
