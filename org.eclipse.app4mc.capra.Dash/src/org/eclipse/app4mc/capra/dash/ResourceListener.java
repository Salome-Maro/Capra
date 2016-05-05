package org.eclipse.app4mc.capra.dash;

import java.util.Optional;
import org.eclipse.app4mc.capra.generic.adapters.TraceMetamodelAdapter;
import org.eclipse.app4mc.capra.generic.adapters.TracePersistenceAdapter;
import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapper;
import org.eclipse.app4mc.capra.generic.artifacts.ArtifactWrapperContainer;
import org.eclipse.app4mc.capra.generic.helpers.ExtensionPointHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;


public class ResourceListener implements IResourceChangeListener{

	final static int ARTIFACT_RENAMED = 0;
	final static int ARTIFACT_MOVED = 1;
	final static int ARTIFACT_DELETED = 2;

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		IResourceDelta delta = event.getDelta();

		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {

				IPath toPath = delta.getMovedToPath();
				

				
				if(delta.getKind() == IResourceDelta.REMOVED && toPath!=null) {

					if(delta.getFullPath().toFile().getName().equalsIgnoreCase(toPath.toFile().getName()))
						markupJob(delta, ARTIFACT_MOVED);
					else markupJob(delta, ARTIFACT_RENAMED);
				}
				if(delta.getKind() == IResourceDelta.REMOVED && toPath==null){
					markupJob(delta, ARTIFACT_DELETED);
				}
				return true;
			}
		};
		try {
			delta.accept(visitor);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void markupJob(IResourceDelta delta, int issueType){


		WorkspaceJob job = new WorkspaceJob("myJob") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					ResourceSet resourceSet = new ResourceSetImpl();
					TracePersistenceAdapter tracePersistenceAdapter = ExtensionPointHelper.getTracePersistenceAdapter().get();
					Optional<ArtifactWrapperContainer> awc = tracePersistenceAdapter.getArtifactWrappers(resourceSet);
					Optional<EObject> tracemodel = tracePersistenceAdapter.getTraceModel(resourceSet);
					if(! tracemodel.isPresent() || ! awc.isPresent()) return Status.OK_STATUS;
					TraceMetamodelAdapter tracemetamodeladapter = ExtensionPointHelper.getTraceMetamodelAdapter().get();

					URI uri = EcoreUtil.getURI(tracemodel.get());
					IPath path = new Path(uri.toPlatformString(false));
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
					EList<ArtifactWrapper> list = awc.get().getArtifacts();
					for (ArtifactWrapper aw : list) {
					
						if(aw.getUri().toString().equals(delta.getResource().getFullPath().toString())){

							if (tracemetamodeladapter.getConnectedElements(aw, tracemodel).size() > 0) {
								IMarker marker = file.createMarker("org.eclipse.app4mc.capra.Dash.mytracemarker");

								if(issueType == ARTIFACT_RENAMED){
									marker.setAttribute(IMarker.MESSAGE, delta.getFullPath()
											+ " has been renamed to " + delta.getMovedToPath());
								}else 
									if(issueType == ARTIFACT_MOVED){
										marker.setAttribute(IMarker.MESSAGE, delta.getResource()
												.getName() + " has been moved from " + delta.getFullPath()
												+ " to " + delta.getMovedToPath());
									}else
										if(issueType == ARTIFACT_DELETED){
											marker.setAttribute(IMarker.MESSAGE, delta.getResource()
													.getName() + " has been deleted from " + delta.getFullPath());
										}
								marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
							}
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
