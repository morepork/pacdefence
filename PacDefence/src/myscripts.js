// document.write("<script src=\"http://java.com/js/deployJava.js\"></script>");

function writeNavBar() {
	document.write("<ul class=\"navbar\">");
	writeNavBarLine("index.html", "Main");
	writeNavBarLine("pacdefence.html", "Pac Defence");
	writeNavBarLine("debug.html", "Pac Defence (debug)");
	writeNavBarLine("changelog.html", "Change Log");
	writeNavBarLine("screenshots.html", "Screenshots");
	document.write("<li><a href=\"PacDefence.jar\">Download</a>");
	document.write("<li><a href=\"PacDefenceSrc.jar\">Download (with source)</a>");
	document.write("</ul>");
	
	// Writes one link to the nav bar checking if it is the current page, and if so making it bold
	function writeNavBarLine(path, name) {
		document.write("<li><a href=\"" + path + "\">");
		url = location.href;
		page = url.slice(url.lastIndexOf("/") + 1);
		if(page == path) {
			document.write("<b>" + name + "</b>");
		} else {
			document.write(name);
		}
		document.write("</a>");
	}
}

// You need to externally load the script http://java.com/js/deployJava.js before calling this as I
// haven't figured out how to get this to load it yet
function runPacDefence(debugTimes) {
	deployJava.runApplet({archive:"PacDefence.jar", code:"gui.Applet.class", width:"800", Height:"600"},
			{DebugTimes:debugTimes}, "1.6");
}