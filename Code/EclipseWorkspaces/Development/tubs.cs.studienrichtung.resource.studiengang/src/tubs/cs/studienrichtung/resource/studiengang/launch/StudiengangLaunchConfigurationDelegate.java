/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package tubs.cs.studienrichtung.resource.studiengang.launch;

import java.io.File;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import tubs.cs.studienrichtung.StudyArea;
import tubs.cs.studienrichtung.featuremodel.FMGenerator;
import tubs.cs.studienrichtung.util.MessageConsoleOutput;
import tubs.cs.studienrichtung.util.localization.BritishEnglish;
import tubs.cs.studienrichtung.util.localization.Localization;

/**
 * A class that handles launch configurations.
 */
public class StudiengangLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
	
	/**
	 * The URI of the resource that shall be launched.
	 */
	public final static String ATTR_RESOURCE_URI = "uri";

	private final static String ConsoleName = "BranchOfStudyConsole";
	
	private final FMGenerator fmGenerator = new FMGenerator();
	private final Localization language = new BritishEnglish();

	private MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	}
	
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		StudiengangLaunchConfigurationHelper helper = new StudiengangLaunchConfigurationHelper();
		StudyArea studyArea = (StudyArea) helper.getModelRoot(configuration);
		
		MessageConsole console = findConsole(ConsoleName);
		console.clearConsole();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		File workspaceDirectory = workspace.getRoot().getLocation().toFile();
		
		URI studyAreaPath = helper.getURI(configuration);
		String studyAreaFileString = studyAreaPath.toPlatformString(false);
		File currentDirectory = new File(workspaceDirectory, studyAreaFileString).getParentFile();
		
		fmGenerator.initialize(language, new MessageConsoleOutput(console), currentDirectory);
		fmGenerator.parse(studyArea);
		fmGenerator.evaluateEncodingsOn(studyArea);
	}	
}
