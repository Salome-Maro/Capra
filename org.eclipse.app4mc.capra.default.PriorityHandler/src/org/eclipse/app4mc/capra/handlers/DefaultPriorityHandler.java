package org.eclipse.app4mc.capra.handlers;

import java.util.Collection;

import org.eclipse.app4mc.capra.generic.handlers.ArtifactHandler;
import org.eclipse.app4mc.capra.generic.handlers.PriorityHandler;
import org.eclipse.mylyn.builds.internal.core.BuildElement;
import org.eclipse.mylyn.builds.internal.core.TestElement;

public class DefaultPriorityHandler implements PriorityHandler{

	@Override
	public ArtifactHandler getSelectedHandler(Collection<ArtifactHandler> handlers, Object selectedElement) {
		if (selectedElement instanceof TestElement || selectedElement instanceof BuildElement)
		{
			return handlers.stream().filter(h -> h instanceof HudsonHandler).findAny().get();
			
		}
		return null;
	}

}
