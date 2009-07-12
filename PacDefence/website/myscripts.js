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

// You need to externally load the script http://java.com/js/deployJava.js before calling this as I
// haven't figured out how to get this to load it yet
function runPacDefence(debugTimes) {
	deployJava.runApplet(
			{archive:"PacDefence.jar", code:"gui.Applet.class", width:"800", Height:"600"},
			{DebugTimes:debugTimes}, "1.6");
}