module se.uu.ub.cora.xmlutils {
	requires transitive java.xml;

	exports se.uu.ub.cora.xmlutils.transformer;

	opens place;
	opens xslt;
}