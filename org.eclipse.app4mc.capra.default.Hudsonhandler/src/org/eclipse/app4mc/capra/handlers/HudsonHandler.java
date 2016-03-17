package org.eclipse.app4mc.capra.handlers;

import java.util.Optional;

import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapper;
import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapperContainer;
import org.eclipse.app4mc.capra.generic.artifacts.ArtifactsFactory;
import org.eclipse.app4mc.capra.generic.handlers.ArtifactHandler;
import org.eclipse.app4mc.capra.generic.helpers.ArtifactHelper;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.builds.internal.core.BuildElement;
import org.eclipse.mylyn.builds.internal.core.TestElement;

public class HudsonHandler implements ArtifactHandler {

	@Override
	public boolean canHandleSelection(Object selection) {
		return (selection instanceof TestElement || selection instanceof BuildElement);
	}

	@Override
	public EObject getEObjectForSelection(Object selection, Optional<ArtifactWrapperContainer> existingWrappers) {
		if (selection instanceof TestElement) {
			TestElement test = (TestElement) selection;
			ArtifactWrapper testWrapper = ArtifactsFactory.eINSTANCE.createArtifactWrapper();

			testWrapper.setName(test.getLabel());
			testWrapper.setUri(test.getLabel()); //TODO Need to get the URI for where the test is
			testWrapper.setArtifactHandler(this.getClass().getName());
			return ArtifactHelper.existingWrapperWithURIorNew(testWrapper, existingWrappers);
		} 
		else if (selection instanceof BuildElement) {
			BuildElement build = (BuildElement) selection;
			ArtifactWrapper buildWrapper = ArtifactsFactory.eINSTANCE.createArtifactWrapper();

			buildWrapper.setName(build.getLabel());
			buildWrapper.setUri(build.getUrl());
			buildWrapper.setArtifactHandler(this.getClass().getName());
			return ArtifactHelper.existingWrapperWithURIorNew(buildWrapper, existingWrappers);
		}

		return null;
	}

}
