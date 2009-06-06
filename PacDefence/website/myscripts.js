/*
 *  This file is part of Pac Defence.
 *
 *  Pac Defence is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Pac Defence is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Liam Byrne, 2008 - 09.
 */

function writeNavBar() {
	document.write("<ul class=\"navbar\">");
	writeNavBarLine("index.html", "Main");
	writeNavBarLine("pacdefence.html", "Pac Defence");
	writeNavBarLine("debug.html", "Pac Defence (debug)");
	writeNavBarLine("changelog.html", "Change Log");
	writeNavBarLine("screenshots.html", "Screenshots");
	document.write('<li><a href="PacDefence.jar">Download</a>');
	document.write('<li><a href="PacDefenceSrc.jar">Download (with source)</a>');
	document.write('<li><a href="PacDefence_manual.pdf">Manual (unfinished)</a>');
	document.write("</ul>");
	
	// Writes one link to the nav bar checking if it is the current page, and if so making it bold
	function writeNavBarLine(path, name) {
		document.write("<li><a href=\"" + path + "\">");
		var url = location.href;
		var page = url.slice(url.lastIndexOf("/") + 1);
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