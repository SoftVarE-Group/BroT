SYNTAXDEF studiengang
FOR <http://tubs.cs/studienrichtung/1.0>
START StudyArea

OPTIONS {
	reloadGeneratorModel = "true";
	overrideLaunchConfigurationDelegate = "false";
}

TOKENS {
	DEFINE IDENTIFIER_TOKEN $('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*$;
	DEFINE INTEGER_TOKEN $('-')?('0'..'9')+$;
	// ( and ) are not allowed in FeatureIDE feature names
	DEFINE NAME_TOKEN $ '"' ('a'..'z'|'A'..'Z'|'_'|'-'|'&'|'ä'|'ö'|'ü'|'ß'|'.'|','|':'|'0'..'9'|$ + WHITESPACE + $)* '"'$;
	
	DEFINE SL_COMMENT $'//'(~('\n'|'\r'|'\uffff'))*$;
	DEFINE ML_COMMENT $'/*'.*'*/'$;
}

TOKENSTYLES {
	"IDENTIFIER_TOKEN" COLOR #6A3E3E;
	"INTEGER_TOKEN", "NAME_TOKEN" COLOR #0000C0;
	"SL_COMMENT", "ML_COMMENT" COLOR #3F7F5F;
}

RULES {
	StudyArea ::= ("Field" | "Studiengang") name[NAME_TOKEN] (branches | categories)*;
	
	Category ::= ("Category" | "Bereich") name[NAME_TOKEN]
					("[" cardinalityMin[INTEGER_TOKEN] ";" cardinalityMax[INTEGER_TOKEN] "]")?
					"{" (categories | subjects)* "}";
	Subject ::= name[NAME_TOKEN] creditPoints[INTEGER_TOKEN] ("LP" | "CP");
	
	Branch ::= ("Branch" | "Studienrichtung") name[NAME_TOKEN] constraints*;
	
	Mandatory ::= ("Pflicht" | "Compulsory") subjects[NAME_TOKEN]*;
	Optional ::= ("Wahlpflicht" | "CompulsoryElective") creditsToAchieve[INTEGER_TOKEN] ("LP" | "CP") subjects[NAME_TOKEN]*;
	
	/// We ignore this case for now
	//MandatoryOptionals ::= "Pflichtwahl" numberOfMandatorySubjects[INTEGER_TOKEN] subjects[NAME_TOKEN]*;
}
