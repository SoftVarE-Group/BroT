package tubs.cs.studienrichtung.util.localization;

import tubs.cs.studienrichtung.*;

public class German extends Localization {
	@Override
	protected void initialize() {
		names.put(StudyArea.class, "Studiengang");
		names.put(Branch.class, "Studienrichtung");
		
		names.put(Mandatory.class, "Pflicht");
		names.put(Optional.class, "Wahlpflicht");
		
		names.put(Subject.class, "Leistung");
		
		names.put(Category.class, "Bereich");
	}
}
