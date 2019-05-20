/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package tubs.cs.studienrichtung.resource.studiengang.analysis;

import java.util.Map;
import org.eclipse.emf.ecore.EReference;

public class ConstraintSubjectsReferenceResolver implements tubs.cs.studienrichtung.resource.studiengang.IStudiengangReferenceResolver<tubs.cs.studienrichtung.Constraint, tubs.cs.studienrichtung.Subject> {
	
	private tubs.cs.studienrichtung.resource.studiengang.analysis.StudiengangDefaultResolverDelegate<tubs.cs.studienrichtung.Constraint, tubs.cs.studienrichtung.Subject> delegate = new tubs.cs.studienrichtung.resource.studiengang.analysis.StudiengangDefaultResolverDelegate<tubs.cs.studienrichtung.Constraint, tubs.cs.studienrichtung.Subject>();
	
	public void resolve(String identifier, tubs.cs.studienrichtung.Constraint container, EReference reference, int position, boolean resolveFuzzy, final tubs.cs.studienrichtung.resource.studiengang.IStudiengangReferenceResolveResult<tubs.cs.studienrichtung.Subject> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(tubs.cs.studienrichtung.Subject element, tubs.cs.studienrichtung.Constraint container, EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
