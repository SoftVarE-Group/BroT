/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package tubs.cs.studienrichtung.resource.studiengang.analysis;

import java.util.Map;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class StudiengangINTEGER_TOKENTokenResolver implements tubs.cs.studienrichtung.resource.studiengang.IStudiengangTokenResolver {
	
	private tubs.cs.studienrichtung.resource.studiengang.analysis.StudiengangDefaultTokenResolver defaultTokenResolver = new tubs.cs.studienrichtung.resource.studiengang.analysis.StudiengangDefaultTokenResolver(true);
	
	public String deResolve(Object value, EStructuralFeature feature, EObject container) {
		// By default token de-resolving is delegated to the DefaultTokenResolver.
		String result = defaultTokenResolver.deResolve(value, feature, container, null, null, null);
		return result;
	}
	
	public void resolve(String lexem, EStructuralFeature feature, tubs.cs.studienrichtung.resource.studiengang.IStudiengangTokenResolveResult result) {
		// By default token resolving is delegated to the DefaultTokenResolver.
		defaultTokenResolver.resolve(lexem, feature, result, null, null, null);
	}
	
	public void setOptions(Map<?,?> options) {
		defaultTokenResolver.setOptions(options);
	}
	
}
