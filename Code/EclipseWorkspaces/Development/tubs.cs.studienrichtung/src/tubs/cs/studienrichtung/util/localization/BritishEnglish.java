package tubs.cs.studienrichtung.util.localization;

import tubs.cs.studienrichtung.Branch;
import tubs.cs.studienrichtung.Category;
import tubs.cs.studienrichtung.Mandatory;
import tubs.cs.studienrichtung.Optional;
import tubs.cs.studienrichtung.StudyArea;
import tubs.cs.studienrichtung.Subject;

public class BritishEnglish extends Localization {
	@Override
	protected void initialize() {
		names.put(StudyArea.class, "Field");
		names.put(Branch.class, "Branch of Study");
		
		names.put(Mandatory.class, "Compulsory");
		names.put(Optional.class, "Compulsory Elective");
		
		names.put(Subject.class, "Subject");
		
		names.put(Category.class, "Category");
	}
}
