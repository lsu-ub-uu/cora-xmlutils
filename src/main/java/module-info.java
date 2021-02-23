module se.uu.ub.cora.xmlutils {
	exports se.uu.ub.cora.xmlutils.transformer;

	// requires se.uu.ub.cora.converter;
	requires transitive java.xml;
	// requires se.uu.ub.cora.data;

	opens place;
	opens xslt;
}